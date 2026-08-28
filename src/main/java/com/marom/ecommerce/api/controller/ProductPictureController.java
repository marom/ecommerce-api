package com.marom.ecommerce.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.marom.ecommerce.api.dto.ErrorResponse;
import com.marom.ecommerce.api.dto.ProductPictureResponse;
import com.marom.ecommerce.api.dto.ProductPictureUpdateRequest;
import com.marom.ecommerce.api.service.ProductPictureService;
import com.marom.ecommerce.api.service.ProductPictureService.PictureContent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products/{productId}/pictures")
@RequiredArgsConstructor
@Tag(name = "Product pictures", description = "Upload and manage the pictures attached to a product")
public class ProductPictureController {

    private final ProductPictureService productPictureService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload one or more pictures for a product",
            description = "Accepts multiple 'files' parts in a single multipart/form-data request. Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pictures uploaded"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "An uploaded file is too large",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Unsupported content type, empty file, or picture limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ProductPictureResponse>> upload(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productPictureService.addPictures(productId, files));
    }

    @GetMapping
    @Operation(summary = "List all pictures for a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pictures retrieved"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ProductPictureResponse>> getAll(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        return ResponseEntity.ok(productPictureService.getPictures(productId));
    }

    @GetMapping("/{pictureId}/content")
    @Operation(summary = "Download the raw image bytes of a picture")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image bytes"),
            @ApiResponse(responseCode = "404", description = "Product or picture not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<byte[]> content(@Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Picture ID") @PathVariable Long pictureId) {
        PictureContent picture = productPictureService.getPictureContent(productId, pictureId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + contentFilename(picture, pictureId) + "\"")
                .contentType(MediaType.parseMediaType(picture.contentType()))
                .contentLength(picture.sizeBytes())
                .body(picture.data());
    }

    @PutMapping("/{pictureId}")
    @Operation(summary = "Update a picture's metadata (alt text and display order)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Picture updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not an admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product or picture not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductPictureResponse> update(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Picture ID") @PathVariable Long pictureId,
            @Valid @RequestBody ProductPictureUpdateRequest request) {
        return ResponseEntity.ok(productPictureService.updatePicture(productId, pictureId, request));
    }

    @DeleteMapping("/{pictureId}")
    @Operation(summary = "Delete a picture from a product")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Picture deleted"),
            @ApiResponse(responseCode = "404", description = "Product or picture not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Picture ID") @PathVariable Long pictureId) {
        productPictureService.deletePicture(productId, pictureId);
        return ResponseEntity.noContent().build();
    }

    private static String contentFilename(PictureContent picture, Long pictureId) {
        return picture.filename() != null ? picture.filename() : "picture-" + pictureId;
    }
}
