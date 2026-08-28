package com.marom.ecommerce.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.marom.ecommerce.api.dto.ProductPictureResponse;
import com.marom.ecommerce.api.dto.ProductPictureUpdateRequest;
import com.marom.ecommerce.api.entity.Product;
import com.marom.ecommerce.api.entity.ProductPicture;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.ProductPictureRepository;
import com.marom.ecommerce.api.repository.ProductPictureView;
import com.marom.ecommerce.api.repository.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPictureServiceTest {

    @Mock
    private ProductPictureRepository productPictureRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductPictureService productPictureService;

    private static Product product() {
        return Product.builder().id(1L).name("Wireless Mouse").build();
    }

    private static MockMultipartFile image(String filename, String contentType, int size) {
        return new MockMultipartFile("files", filename, contentType, new byte[size]);
    }

    private static ProductPictureView view(long id, int displayOrder, String altText) {
        return new ProductPictureView() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public Long getProductId() {
                return 1L;
            }

            @Override
            public String getAltText() {
                return altText;
            }

            @Override
            public Integer getDisplayOrder() {
                return displayOrder;
            }

            @Override
            public String getContentType() {
                return "image/jpeg";
            }

            @Override
            public Long getSizeBytes() {
                return 3L;
            }

            @Override
            public String getOriginalFilename() {
                return "pic-" + id + ".jpg";
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return LocalDateTime.now();
            }
        };
    }

    @Test
    void should_savePicturesWithSequentialDisplayOrder_when_filesAreValidImages() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));
        when(productPictureRepository.countByProductId(1L)).thenReturn(1L);
        when(productPictureRepository.findMaxDisplayOrder(1L)).thenReturn(0);
        when(productPictureRepository.save(any(ProductPicture.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productPictureRepository.findViewsByProductId(1L))
                .thenReturn(List.of(view(1L, 0, null), view(2L, 1, null), view(3L, 2, null)));

        // Act
        List<ProductPictureResponse> responses = productPictureService.addPictures(1L,
                List.of(image("a.jpg", "image/jpeg", 10), image("b.png", "image/png", 20)));

        // Assert
        ArgumentCaptor<ProductPicture> saved = ArgumentCaptor.forClass(ProductPicture.class);
        verify(productPictureRepository, times(2)).save(saved.capture());
        assertThat(responses).hasSize(3);
        assertThat(saved.getAllValues()).extracting(ProductPicture::getDisplayOrder, ProductPicture::getContentType,
                        ProductPicture::getSizeBytes, ProductPicture::getOriginalFilename)
                .containsExactly(
                        tuple(1, "image/jpeg", 10L, "a.jpg"),
                        tuple(2, "image/png", 20L, "b.png"));
    }

    @Test
    void should_throwResourceNotFoundException_when_productDoesNotExist() {
        // Arrange
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productPictureService.addPictures(2L, List.of(image("a.jpg", "image/jpeg", 10))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("2");
        verify(productPictureRepository, never()).save(any());
    }

    @Test
    void should_throwBusinessRuleException_when_noFilesProvided() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));

        // Act & Assert
        assertThatThrownBy(() -> productPictureService.addPictures(1L, List.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("At least one picture file");
        verify(productPictureRepository, never()).save(any());
    }

    @Test
    void should_throwBusinessRuleException_when_contentTypeIsNotAnAllowedImage() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));
        when(productPictureRepository.countByProductId(1L)).thenReturn(0L);

        // Act & Assert
        assertThatThrownBy(() -> productPictureService.addPictures(1L,
                List.of(image("notes.txt", "text/plain", 10))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Unsupported picture content type");
        verify(productPictureRepository, never()).save(any());
    }

    @Test
    void should_throwBusinessRuleException_when_fileExceedsSizeLimit() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));
        when(productPictureRepository.countByProductId(1L)).thenReturn(0L);
        int oversize = (int) (ProductPictureService.MAX_FILE_SIZE_BYTES + 1);

        // Act & Assert
        assertThatThrownBy(() -> productPictureService.addPictures(1L,
                List.of(image("huge.jpg", "image/jpeg", oversize))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("limit");
        verify(productPictureRepository, never()).save(any());
    }

    @Test
    void should_throwBusinessRuleException_when_totalPictureCountWouldExceedMax() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));
        when(productPictureRepository.countByProductId(1L)).thenReturn(9L);

        // Act & Assert
        assertThatThrownBy(() -> productPictureService.addPictures(1L,
                List.of(image("a.jpg", "image/jpeg", 10), image("b.jpg", "image/jpeg", 10))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("at most " + ProductPictureService.MAX_PICTURES_PER_PRODUCT);
        verify(productPictureRepository, never()).save(any());
    }

    @Test
    void should_returnPicturesOrderedWithContentUrls_when_gettingPicturesForProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));
        when(productPictureRepository.findViewsByProductId(1L))
                .thenReturn(List.of(view(4L, 0, "front"), view(7L, 1, "back")));

        // Act
        List<ProductPictureResponse> responses = productPictureService.getPictures(1L);

        // Assert
        assertThat(responses).extracting(ProductPictureResponse::getId, ProductPictureResponse::getUrl)
                .containsExactly(
                        tuple(4L, "/api/v1/products/1/pictures/4/content"),
                        tuple(7L, "/api/v1/products/1/pictures/7/content"));
    }

    @Test
    void should_returnRawBytes_when_pictureContentExists() {
        // Arrange
        ProductPicture picture = ProductPicture.builder().id(5L).data(new byte[] {1, 2, 3})
                .contentType("image/png").sizeBytes(3L).originalFilename("p.png").build();
        when(productPictureRepository.findByIdAndProductId(5L, 1L)).thenReturn(Optional.of(picture));

        // Act
        ProductPictureService.PictureContent content = productPictureService.getPictureContent(1L, 5L);

        // Assert
        assertThat(content.contentType()).isEqualTo("image/png");
        assertThat(content.sizeBytes()).isEqualTo(3L);
        assertThat(content.data()).containsExactly(1, 2, 3);
    }

    @Test
    void should_throwResourceNotFoundException_when_pictureContentIsMissing() {
        // Arrange
        when(productPictureRepository.findByIdAndProductId(9L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productPictureService.getPictureContent(1L, 9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("9");
    }

    @Test
    void should_updateAltTextAndDisplayOrder_when_pictureExists() {
        // Arrange
        ProductPicture picture = ProductPicture.builder().id(7L).altText("old").displayOrder(3).build();
        when(productPictureRepository.findByIdAndProductId(7L, 1L)).thenReturn(Optional.of(picture));
        when(productPictureRepository.save(any(ProductPicture.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productPictureRepository.findViewsByProductId(1L)).thenReturn(List.of(view(7L, 0, "front")));
        ProductPictureUpdateRequest request = ProductPictureUpdateRequest.builder()
                .altText("front").displayOrder(0).build();

        // Act
        ProductPictureResponse response = productPictureService.updatePicture(1L, 7L, request);

        // Assert
        assertThat(picture.getAltText()).isEqualTo("front");
        assertThat(picture.getDisplayOrder()).isEqualTo(0);
        assertThat(response.getId()).isEqualTo(7L);
    }

    @Test
    void should_throwResourceNotFoundException_when_updatingMissingPicture() {
        // Arrange
        when(productPictureRepository.findByIdAndProductId(8L, 1L)).thenReturn(Optional.empty());
        ProductPictureUpdateRequest request = ProductPictureUpdateRequest.builder().displayOrder(0).build();

        // Act & Assert
        assertThatThrownBy(() -> productPictureService.updatePicture(1L, 8L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("8");
        verify(productPictureRepository, never()).save(any());
    }

    @Test
    void should_deletePicture_when_pictureExists() {
        // Arrange
        when(productPictureRepository.deleteByIdAndProductId(3L, 1L)).thenReturn(1);

        // Act
        productPictureService.deletePicture(1L, 3L);

        // Assert
        verify(productPictureRepository).deleteByIdAndProductId(3L, 1L);
    }

    @Test
    void should_throwResourceNotFoundException_when_deletingMissingPicture() {
        // Arrange
        when(productPictureRepository.deleteByIdAndProductId(404L, 1L)).thenReturn(0);

        // Act & Assert
        assertThatThrownBy(() -> productPictureService.deletePicture(1L, 404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }
}
