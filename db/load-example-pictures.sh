#!/usr/bin/env bash
#
# Replace the placeholder product images seeded by db/example-data.sql with real,
# keyword-matched photos from loremflickr.com (Creative Commons Flickr images;
# picsum.photos is the fallback). Idempotent: for each product it deletes any
# existing pictures, then uploads a fresh set of 2-5.
#
# A single flaky download or product never aborts the run - failures are retried,
# then reported in a summary at the end (non-zero exit if any product is left with
# fewer than 2 pictures).
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

set -uo pipefail

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
    -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" 2>/dev/null \
    | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)"
[ -n "$TOKEN" ] || { echo "login failed - is the API running at $API_BASE ?" >&2; exit 1; }
echo "authenticated as $ADMIN_EMAIL"

auth=(-H "Authorization: Bearer $TOKEN")
lock="${RANDOM}"
summary=""
failed=0

# $1 tags  $2 out-file -> writes a real JPEG (>2 KB) or returns 1 after retries
download_image() {
    local tags="$1" out="$2" attempt
    for attempt in 1 2 3 4 5; do
        lock=$((lock + 1))
        if [ "$attempt" -le 3 ]; then
            curl -fsS -L --max-time 30 -o "$out" \
                "https://loremflickr.com/${IMG_W}/${IMG_H}/${tags}?lock=${lock}" 2>/dev/null || true
        else
            curl -fsS -L --max-time 30 -o "$out" \
                "https://picsum.photos/seed/${tags//[^a-z0-9]/}-${lock}/${IMG_W}/${IMG_H}" 2>/dev/null || true
        fi
        if [ -s "$out" ] && file -b "$out" 2>/dev/null | grep -qi 'JPEG' \
           && [ "$(stat -c%s "$out")" -gt 2000 ]; then
            return 0
        fi
        sleep 1
    done
    return 1
}

# $1 product_id -> delete every current picture via the API
delete_existing() {
    local pid="$1" ids id n=0
    ids="$(curl -fsS "$API/products/$pid/pictures" 2>/dev/null | grep -o '"id":[0-9]*' | cut -d: -f2)"
    for id in $ids; do
        curl -fsS -o /dev/null "${auth[@]}" -X DELETE "$API/products/$pid/pictures/$id" 2>/dev/null && n=$((n + 1))
    done
    [ "$n" -gt 0 ] && echo "  cleared $n existing picture(s)"
    return 0
}

while IFS='|' read -r pid count tags slug; do
    [ -n "${pid:-}" ] || continue
    echo "product $pid ($slug): want $count picture(s)"
    delete_existing "$pid"

    form=()
    got=0
    for i in $(seq 1 "$count"); do
        f="$TMP/${slug}-${i}.jpg"
        if download_image "$tags" "$f"; then
            printf '  downloaded %s (%s bytes)\n' "$(basename "$f")" "$(stat -c%s "$f")"
            form+=(-F "files=@${f};type=image/jpeg;filename=${slug}-${i}.jpg")
            got=$((got + 1))
        else
            echo "  !! gave up fetching a JPEG for slot $i" >&2
        fi
        sleep 0.5
    done

    if [ "$got" -eq 0 ]; then
        echo "  no images downloaded - skipping upload" >&2
        summary+=$'\n'"  product $pid ($slug): 0 uploaded  [FAILED]"
        failed=1
        continue
    fi

    code="$(curl -fsS -o "$TMP/resp.json" -w '%{http_code}' "${auth[@]}" \
        -X POST "$API/products/$pid/pictures" "${form[@]}" 2>/dev/null || true)"
    if [ "$code" = "201" ]; then
        echo "  uploaded $got -> 201"
        summary+=$'\n'"  product $pid ($slug): $got uploaded"
    else
        echo "  upload FAILED ($code): $(cat "$TMP/resp.json" 2>/dev/null)" >&2
        summary+=$'\n'"  product $pid ($slug): upload failed (HTTP $code)  [FAILED]"
        failed=1
    fi
done <<< "$CATALOG"

echo
echo "=== summary ===$summary"
echo
echo "picture counts now:"
short=0
for pid in 1 2 3 4 5 6 7 8 9 10; do
    n="$(curl -fsS "$API/products/$pid/pictures" 2>/dev/null | grep -o '"id":[0-9]*' | wc -l)"
    printf '  product %-2s : %s\n' "$pid" "$n"
    [ "$n" -lt 2 ] && short=1
done

if [ "$failed" -ne 0 ] || [ "$short" -ne 0 ]; then
    echo
    echo "some products are short on pictures - re-run the script to fill them in." >&2
    exit 1
fi
echo
echo "all products have 2-5 pictures."
