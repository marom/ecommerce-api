package com.marom.ecommerce.api.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marom.ecommerce.api.dto.ProductPictureResponse;
import com.marom.ecommerce.api.dto.ProductPictureUpdateRequest;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.service.ProductPictureService;
import com.marom.ecommerce.api.service.ProductPictureService.PictureContent;
import com.marom.ecommerce.api.support.SecurityTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductPictureController.class)
@Import(SecurityTestSupport.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WithMockUser(roles = "ADMIN")
class ProductPictureControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductPictureService productPictureService;

    ProductPictureControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private static MockMultipartFile file(String name) {
        return new MockMultipartFile("files", name, "image/jpeg", new byte[] {1, 2, 3});
    }

    private static ProductPictureResponse response(long id, int displayOrder) {
        return ProductPictureResponse.builder()
                .id(id).productId(1L).url("/api/v1/products/1/pictures/" + id + "/content")
                .displayOrder(displayOrder).contentType("image/jpeg").sizeBytes(3L).originalFilename("p.jpg")
                .build();
    }

    // ----- POST /api/v1/products/{productId}/pictures -----

    @Test
    void should_returnCreated_when_adminUploadsPictures() throws Exception {
        // Arrange
        when(productPictureService.addPictures(eq(1L), anyList()))
                .thenReturn(List.of(response(1L, 0), response(2L, 1)));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/products/1/pictures").file(file("a.jpg")).file(file("b.jpg")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].url").value("/api/v1/products/1/pictures/1/content"))
                .andExpect(jsonPath("$[1].displayOrder").value(1));
    }

    @Test
    void should_returnNotFound_when_uploadingToMissingProduct() throws Exception {
        // Arrange
        when(productPictureService.addPictures(eq(404L), anyList()))
                .thenThrow(new ResourceNotFoundException("Product not found with id 404"));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/products/404/pictures").file(file("a.jpg")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void should_return403_when_customerUploadsPictures() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/v1/products/1/pictures").file(file("a.jpg")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void should_return401_when_anonymousUploadsPictures() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/v1/products/1/pictures").file(file("a.jpg")))
                .andExpect(status().isUnauthorized());
    }

    // ----- GET /api/v1/products/{productId}/pictures -----

    @Test
    @WithAnonymousUser
    void should_returnOk_when_anyoneListsPictures() throws Exception {
        // Arrange
        when(productPictureService.getPictures(1L)).thenReturn(List.of(response(1L, 0)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1/pictures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void should_returnNotFound_when_listingPicturesForMissingProduct() throws Exception {
        // Arrange
        when(productPictureService.getPictures(404L))
                .thenThrow(new ResourceNotFoundException("Product not found with id 404"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/404/pictures"))
                .andExpect(status().isNotFound());
    }

    // ----- GET /api/v1/products/{productId}/pictures/{pictureId}/content -----

    @Test
    @WithAnonymousUser
    void should_returnRawBytesWithContentType_when_pictureContentExists() throws Exception {
        // Arrange
        when(productPictureService.getPictureContent(1L, 1L))
                .thenReturn(new PictureContent(new byte[] {1, 2, 3}, "image/png", "front.png", 3L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1/pictures/1/content"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"front.png\""))
                .andExpect(content().bytes(new byte[] {1, 2, 3}));
    }

    @Test
    void should_returnNotFound_when_pictureContentIsMissing() throws Exception {
        // Arrange
        when(productPictureService.getPictureContent(1L, 99L))
                .thenThrow(new ResourceNotFoundException("Picture not found with id 99 for product with id 1"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1/pictures/99/content"))
                .andExpect(status().isNotFound());
    }

    // ----- PUT /api/v1/products/{productId}/pictures/{pictureId} -----

    @Test
    void should_returnOk_when_pictureMetadataUpdateIsValid() throws Exception {
        // Arrange
        ProductPictureUpdateRequest request = ProductPictureUpdateRequest.builder()
                .altText("front").displayOrder(0).build();
        when(productPictureService.updatePicture(eq(1L), eq(1L), any(ProductPictureUpdateRequest.class)))
                .thenReturn(response(1L, 0));

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1/pictures/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void should_returnBadRequest_when_displayOrderIsMissingOnUpdate() throws Exception {
        // Arrange
        ProductPictureUpdateRequest request = ProductPictureUpdateRequest.builder().altText("front").build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1/pictures/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_displayOrderIsNegativeOnUpdate() throws Exception {
        // Arrange
        ProductPictureUpdateRequest request = ProductPictureUpdateRequest.builder().displayOrder(-1).build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1/pictures/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ----- DELETE /api/v1/products/{productId}/pictures/{pictureId} -----

    @Test
    void should_returnNoContent_when_pictureIsDeleted() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/1/pictures/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_returnNotFound_when_deletingMissingPicture() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Picture not found with id 99 for product with id 1"))
                .when(productPictureService).deletePicture(1L, 99L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/1/pictures/99"))
                .andExpect(status().isNotFound());
    }
}
