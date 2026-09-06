package dev.andreasgeorgatos.pointofservicebackend.controllers;

import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductCreateRequest;
import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.enums.Category;
import dev.andreasgeorgatos.pointofservicebackend.exceptions.DuplicateResourceException;
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
    private static final long MISSING_INGREDIENT_ID = 404L;
    private static final String NAME = "Espresso";
    private static final String DESCRIPTION = "A double shot of arabica.";
    private static final String IMAGE = "https://cdn.example.com/espresso.png";
    private static final Category CATEGORY = Category.BEVERAGES;
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
        request = new ProductCreateRequest(NAME, DESCRIPTION, IMAGE, Set.of(1L), CATEGORY, PRICE);
    }

    @Test
    @DisplayName("returns 200 with every product the service reports, in order")
    void getAllProducts_returnsAllProducts() {
        when(productService.getAllProducts()).thenReturn(List.of(firstProduct, secondProduct));

        ResponseEntity<List<ProductResponseDTO>> response = productController.getAllProducts();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().containsExactly(firstProduct, secondProduct);

        verify(productService).getAllProducts();
        verifyNoMoreInteractions(productService);
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

        assertThatThrownBy(() -> productController.getProduct(MISSING_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(MISSING_ID));
    }

    @Test
    @DisplayName("returns 201 with the created product")
    void createProduct_validRequest_returnsCreatedProduct() {
        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(product);

        ResponseEntity<Product> response = productController.createProduct(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
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
        assertThat(captured.ingredientIds()).containsExactly(1L);
        assertThat(captured.price()).isEqualByComparingTo(PRICE);
    }

    @Test
    @DisplayName("propagates the service's rejection of a duplicate product")
    void createProduct_duplicateName_propagatesDuplicateResource() {
        when(productService.createProduct(any(ProductCreateRequest.class))).thenThrow(new DuplicateResourceException("Product already exists: " + NAME));

        assertThatThrownBy(() -> productController.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(NAME);
    }

    @Test
    @DisplayName("propagates EntityNotFoundException when an ingredient id cannot be resolved")
    void createProduct_unknownIngredientId_propagatesEntityNotFound() {
        when(productService.createProduct(any(ProductCreateRequest.class))).thenThrow(new EntityNotFoundException("Ingredient not found: " + MISSING_INGREDIENT_ID));

        assertThatThrownBy(() -> productController.createProduct(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Ingredient not found")
                .hasMessageContaining(String.valueOf(MISSING_INGREDIENT_ID));
    }

    @Test
    @DisplayName("returns 200 with the updated product")
    void updateProduct_existingId_returnsUpdatedProduct() {
        ProductCreateRequest updateRequest = new ProductCreateRequest("Espresso Doppio", DESCRIPTION, IMAGE, request.ingredientIds(), CATEGORY, new BigDecimal("3.80"));

        when(productService.updateProduct(eq(PRODUCT_ID), any(ProductCreateRequest.class))).thenReturn(updatedProduct);

        ResponseEntity<Product> response = productController.updateProduct(PRODUCT_ID, updateRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updatedProduct);
    }

    @Test
    @DisplayName("passes the path id and the submitted body, not the original, to the service")
    void updateProduct_passesIdAndRequestToService() {
        ProductCreateRequest updateRequest = new ProductCreateRequest("Espresso Doppio", DESCRIPTION, IMAGE, Set.of(2L, 3L), CATEGORY, new BigDecimal("3.80"));

        when(productService.updateProduct(eq(PRODUCT_ID), any(ProductCreateRequest.class))).thenReturn(updatedProduct);

        productController.updateProduct(PRODUCT_ID, updateRequest);

        verify(productService).updateProduct(eq(PRODUCT_ID), requestCaptor.capture());

        ProductCreateRequest captured = requestCaptor.getValue();

        assertThat(captured).isSameAs(updateRequest).isNotSameAs(request);
        assertThat(captured.name()).isEqualTo("Espresso Doppio");
        assertThat(captured.ingredientIds()).containsExactlyInAnyOrder(2L, 3L);
        assertThat(captured.price()).isEqualByComparingTo(new BigDecimal("3.80"));
    }

    @Test
    @DisplayName("propagates EntityNotFoundException when updating a product that does not exist")
    void updateProduct_missingId_propagatesEntityNotFound() {
        when(productService.updateProduct(eq(MISSING_ID), any(ProductCreateRequest.class))).thenThrow(new EntityNotFoundException("Product not found: " + MISSING_ID));

        assertThatThrownBy(() -> productController.updateProduct(MISSING_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(MISSING_ID));
    }

    @Test
    @DisplayName("propagates EntityNotFoundException when an update names an unknown ingredient")
    void updateProduct_unknownIngredientId_propagatesEntityNotFound() {
        when(productService.updateProduct(eq(PRODUCT_ID), any(ProductCreateRequest.class))).thenThrow(new EntityNotFoundException("Ingredient not found: " + MISSING_INGREDIENT_ID));

        assertThatThrownBy(() -> productController.updateProduct(PRODUCT_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Ingredient not found");
    }

    @Test
    @DisplayName("returns 204 with no body after deleting")
    void deleteProduct_existingId_returnsNoContent() {
        ResponseEntity<Void> response = productController.deleteProduct(PRODUCT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(productService).deleteProduct(PRODUCT_ID);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("propagates EntityNotFoundException when deleting a product that does not exist")
    void deleteProduct_missingId_propagatesEntityNotFound() {
        doThrow(new EntityNotFoundException("Product not found: " + MISSING_ID)).when(productService).deleteProduct(MISSING_ID);

        assertThatThrownBy(() -> productController.deleteProduct(MISSING_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(MISSING_ID));
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

    @Test
    @DisplayName("listing products never mutates them")
    void getAllProducts_doesNotTouchWriteOperations() {
        when(productService.getAllProducts()).thenReturn(List.of(firstProduct));

        productController.getAllProducts();

        verify(productService, never()).createProduct(any());
        verify(productService, never()).updateProduct(any(), any());
        verify(productService, never()).deleteProduct(any());
    }
}
