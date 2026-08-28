#!/usr/bin/env bash
#
# Replace the placeholder product images seeded by db/example-data.sql with real,
# keyword-matched photos from loremflickr.com (Creative Commons Flickr images;
# picsum.photos is the fallback). Idempotent: for each product it deletes any
# existing pictures, then uploads a fresh set of 2-5.
#
# Requires: a running API with the demo data loaded, plus curl and file(1).
# Assumes product ids 1-10 as created by db/example-data.sql.
#
# Usage:
#   ./db/load-example-pictures.sh
#   API_BASE=http://localhost:18080 ./db/load-example-pictures.sh
#
# Env vars (all optional):
#   API_BASE        base URL of the API           (default http://localhost:8080)
#   ADMIN_EMAIL     admin account for the token   (default admin@shop.example.com)
#   ADMIN_PASSWORD  admin password                (default admin123)
#   IMG_W, IMG_H    downloaded image dimensions   (default 1024x768)

set -euo pipefail

API_BASE="${API_BASE:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@shop.example.com}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
IMG_W="${IMG_W:-1024}"
IMG_H="${IMG_H:-768}"
API="${API_BASE%/}/api/v1"

command -v curl >/dev/null || { echo "curl is required" >&2; exit 1; }
command -v file >/dev/null || { echo "file(1) is required" >&2; exit 1; }

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# product_id | how many pictures | loremflickr tags | filename slug
CATALOG='
1|4|computer,mouse|wireless-mouse
2|5|mechanical,keyboard|mechanical-keyboard
3|3|usb,adapter|usb-c-hub
4|5|headphones|noise-cancelling-headphones
5|2|book,code|clean-code
6|2|book|pragmatic-programmer
7|4|t-shirt|cotton-t-shirt
8|3|hoodie|hooded-sweatshirt
9|3|water,bottle|stainless-steel-bottle
10|4|coffee,mug|ceramic-coffee-mug
'

echo "API: $API"
TOKEN="$(curl -fsS -X POST "$API/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" \
    | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)"
[ -n "$TOKEN" ] || { echo "login failed" >&2; exit 1; }
echo "authenticated as $ADMIN_EMAIL"

auth=(-H "Authorization: Bearer $TOKEN")
lock=100

download_image() {
    # $1 tags  $2 out-file
    local tags="$1" out="$2"
    lock=$((lock + 1))
    curl -fsS -L --max-time 30 -o "$out" "https://loremflickr.com/${IMG_W}/${IMG_H}/${tags}?lock=${lock}" || true
    if ! file -b "$out" 2>/dev/null | grep -qi 'JPEG'; then
        curl -fsS -L --max-time 30 -o "$out" "https://picsum.photos/seed/${tags//,/}-${lock}/${IMG_W}/${IMG_H}"
    fi
    file -b "$out" | grep -qi 'JPEG'
}

delete_existing() {
    # $1 product_id — remove every current picture via the API
    local pid="$1" ids
    ids="$(curl -fsS "$API/products/$pid/pictures" | grep -o '"id":[0-9]*' | cut -d: -f2)"
    for id in $ids; do
        curl -fsS -o /dev/null "${auth[@]}" -X DELETE "$API/products/$pid/pictures/$id"
    done
    [ -n "$ids" ] && echo "  cleared $(echo "$ids" | wc -w) existing picture(s)"
}

while IFS='|' read -r pid count tags slug; do
    [ -n "${pid:-}" ] || continue
    echo "product $pid ($slug): $count picture(s)"
    delete_existing "$pid"

    form=()
    for i in $(seq 1 "$count"); do
        f="$TMP/${slug}-${i}.jpg"
        if download_image "$tags" "$f"; then
            printf '  downloaded %s (%s bytes)\n' "$(basename "$f")" "$(stat -c%s "$f")"
            form+=(-F "files=@${f};type=image/jpeg;filename=${slug}-${i}.jpg")
        else
            echo "  !! could not fetch a JPEG for slot $i, skipping" >&2
        fi
    done

    code="$(curl -fsS -o "$TMP/resp.json" -w '%{http_code}' "${auth[@]}" \
        -X POST "$API/products/$pid/pictures" "${form[@]}")"
    if [ "$code" = "201" ]; then
        echo "  uploaded -> 201"
    else
        echo "  upload FAILED ($code): $(cat "$TMP/resp.json")" >&2
        exit 1
    fi
done <<< "$CATALOG"

echo
echo "done. Current picture counts:"
for pid in 1 2 3 4 5 6 7 8 9 10; do
    n="$(curl -fsS "$API/products/$pid/pictures" | grep -o '"id":[0-9]*' | wc -l)"
    printf '  product %-2s : %s\n' "$pid" "$n"
done
