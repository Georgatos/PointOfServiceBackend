package dev.andreasgeorgatos.pointofservicebackend.controllers;

import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductCreateRequest;
import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.enums.Category;
import dev.andreasgeorgatos.pointofservicebackend.models.items.Ingredient;
import dev.andreasgeorgatos.pointofservicebackend.models.items.Product;
import dev.andreasgeorgatos.pointofservicebackend.services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController unit tests")
class ProductControllerTest {

    private static final long PRODUCT_ID = 7L;
    private static final long MISSING_ID = 99L;
    private static final String NAME = "Espresso";
    private static final String DESCRIPTION = "A double shot of arabica.";
    private static final String IMAGE = "https://cdn.example.com/espresso.png";
    private static final Category CATEGORY = Category.values()[0];
    private static final BigDecimal PRICE = new BigDecimal("2.50");

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Mock
    private Product product;

    @Mock
    private Product updatedProduct;

    @Mock
    private ProductResponseDTO firstProduct;

    @Mock
    private ProductResponseDTO secondProduct;

    @Captor
    private ArgumentCaptor<ProductCreateRequest> requestCaptor;

    private ProductCreateRequest request;

    @BeforeEach
    void setUp() {
        request = new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, Set.of(mock(Ingredient.class)), CATEGORY, PRICE);
    }

    @Test
    @DisplayName("returns 200 with every product the service reports")
    void getAllProducts_returnsAllProducts() {
        when(productService.getAllProducts()).thenReturn(List.of(firstProduct, secondProduct));

        ResponseEntity<List<ProductResponseDTO>> response = productController.getAllProducts();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().containsExactly(firstProduct, secondProduct);

        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("returns 200 with an empty body when no products exist")
    void getAllProducts_noProducts_returnsEmptyList() {
        when(productService.getAllProducts()).thenReturn(List.of());

        ResponseEntity<List<ProductResponseDTO>> response = productController.getAllProducts();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("returns 200 with the product for the requested id")
    void getProduct_existingId_returnsProduct() {
        when(productService.getProductById(PRODUCT_ID)).thenReturn(product);

        ResponseEntity<Product> response = productController.getProduct(PRODUCT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(product);

        verify(productService).getProductById(PRODUCT_ID);
    }

    @Test
    @DisplayName("propagates EntityNotFoundException when the product does not exist")
    void getProduct_missingId_propagatesEntityNotFound() {
        when(productService.getProductById(MISSING_ID)).thenThrow(new EntityNotFoundException("Product not found: " + MISSING_ID));

        assertThatThrownBy(() -> productController.getProduct(MISSING_ID)).isInstanceOf(EntityNotFoundException.class).hasMessageContaining(String.valueOf(MISSING_ID));
    }

    @Test
    @DisplayName("returns 200 with the created product")
    void createProduct_validRequest_returnsCreatedProduct() {
        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(product);

        ResponseEntity<Product> response = productController.createProduct(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(product);
    }

    @Test
    @DisplayName("passes the submitted request straight through to the service")
    void createProduct_passesRequestToService() {
        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(product);

        productController.createProduct(request);

        verify(productService).createProduct(requestCaptor.capture());

        ProductCreateRequest captured = requestCaptor.getValue();

        assertThat(captured).isSameAs(request);
        assertThat(captured.name()).isEqualTo(NAME);
        assertThat(captured.description()).isEqualTo(DESCRIPTION);
        assertThat(captured.image()).isEqualTo(IMAGE);
        assertThat(captured.category()).isEqualTo(CATEGORY);
        assertThat(captured.ingredients()).hasSize(1);
        assertThat(captured.price()).isEqualByComparingTo(PRICE);
    }

    @Test
    @DisplayName("propagates the service's rejection of a duplicate product")
    void createProduct_serviceRejects_propagatesException() {
        when(productService.createProduct(any(ProductCreateRequest.class))).thenThrow(new IllegalArgumentException("Product already exists: " + NAME));

        assertThatThrownBy(() -> productController.createProduct(request)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(NAME);
    }

    @Test
    @DisplayName("returns 200 with the updated product")
    void updateProduct_existingId_returnsUpdatedProduct() {
        ProductCreateRequest updateRequest = new ProductCreateRequest("Espresso Doppio", DESCRIPTION, IMAGE, request.ingredients(), CATEGORY, new BigDecimal("3.80"));

        when(productService.updateProduct(eq(PRODUCT_ID), any(ProductCreateRequest.class))).thenReturn(updatedProduct);

        ResponseEntity<Product> response = productController.updateProduct(PRODUCT_ID, updateRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updatedProduct);
    }

    @Test
    @DisplayName("passes the path id and the request body to the service")
    void updateProduct_passesIdAndRequestToService() {
        when(productService.updateProduct(eq(PRODUCT_ID), any(ProductCreateRequest.class))).thenReturn(updatedProduct);

        productController.updateProduct(PRODUCT_ID, request);

        verify(productService).updateProduct(eq(PRODUCT_ID), requestCaptor.capture());

        assertThat(requestCaptor.getValue()).isSameAs(request);
    }

    @Test
    @DisplayName("propagates EntityNotFoundException when updating a product that does not exist")
    void updateProduct_missingId_propagatesEntityNotFound() {
        when(productService.updateProduct(eq(MISSING_ID), any(ProductCreateRequest.class))).thenThrow(new EntityNotFoundException("Product not found: " + MISSING_ID));

        assertThatThrownBy(() -> productController.updateProduct(MISSING_ID, request)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("returns 204 with no body after deleting")
    void deleteProduct_existingId_returnsNoContent() {
        ResponseEntity<Void> response = productController.deleteProduct(PRODUCT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(productService).deleteProduct(PRODUCT_ID);
    }

    @Test
    @DisplayName("propagates EntityNotFoundException when deleting a product that does not exist")
    void deleteProduct_missingId_propagatesEntityNotFound() {
        doThrow(new EntityNotFoundException("Product not found: " + MISSING_ID)).when(productService).deleteProduct(MISSING_ID);

        assertThatThrownBy(() -> productController.deleteProduct(MISSING_ID)).isInstanceOf(EntityNotFoundException.class).hasMessageContaining(String.valueOf(MISSING_ID));
    }

    @Test
    @DisplayName("reading a product never mutates it")
    void getProduct_doesNotTouchWriteOperations() {
        when(productService.getProductById(PRODUCT_ID)).thenReturn(product);

        productController.getProduct(PRODUCT_ID);

        verify(productService, never()).createProduct(any());
        verify(productService, never()).updateProduct(any(), any());
        verify(productService, never()).deleteProduct(any());
    }
}