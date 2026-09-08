package dev.andreasgeorgatos.pointofservicebackend.exceptions;

import dev.andreasgeorgatos.pointofservicebackend.controllers.ProductController;
import dev.andreasgeorgatos.pointofservicebackend.controllers.UserController;
import dev.andreasgeorgatos.pointofservicebackend.dto.product.ProductCreateRequest;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.RegistrationRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.services.product.ProductService;
import dev.andreasgeorgatos.pointofservicebackend.services.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiExceptionHandler status mapping")
class ApiExceptionHandlerTest {

    private static final long PRODUCT_ID = 7L;
    private static final long MISSING_ID = 99L;
    private static final long MISSING_INGREDIENT_ID = 404L;
    private static final String EMAIL = "new@example.com";
    private static final String PRODUCT_PATH = "/api/v0/product";
    private static final String REGISTER_PATH = "/api/v0/users/register";

    private static final String VALID_REGISTRATION = "{\"email\":\"new@example.com\",\"password\":\"password123\"}";
    private static final String PRODUCT_BODY = "{\"name\":\"Espresso\",\"description\":\"A double shot.\",\"image\":\"espresso.png\",\"ingredientIds\":[1],\"category\":\"BEVERAGES\",\"price\":2.50}";

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(productService), new UserController(userService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("maps EntityNotFoundException to 404 with the exception message")
    void entityNotFound_isMappedToNotFound() throws Exception {
        when(productService.getProductById(MISSING_ID)).thenThrow(new EntityNotFoundException("Product not found: " + MISSING_ID));

        mockMvc.perform(get(PRODUCT_PATH + "/" + MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Product not found: " + MISSING_ID));
    }

    @Test
    @DisplayName("maps a missing product on update to 404")
    void entityNotFoundOnUpdate_isMappedToNotFound() throws Exception {
        when(productService.updateProduct(eq(MISSING_ID), any(ProductCreateRequest.class)))
                .thenThrow(new EntityNotFoundException("Product not found: " + MISSING_ID));

        mockMvc.perform(put(PRODUCT_PATH + "/" + MISSING_ID).contentType(MediaType.APPLICATION_JSON).content(PRODUCT_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found: " + MISSING_ID));
    }

    @Test
    @DisplayName("maps a missing product on delete to 404")
    void entityNotFoundOnDelete_isMappedToNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Product not found: " + MISSING_ID)).when(productService).deleteProduct(MISSING_ID);

        mockMvc.perform(delete(PRODUCT_PATH + "/" + MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found: " + MISSING_ID));
    }

    @Test
    @DisplayName("maps an unresolvable ingredient id to 404")
    void unknownIngredientId_isMappedToNotFound() throws Exception {
        when(productService.updateProduct(eq(PRODUCT_ID), any(ProductCreateRequest.class)))
                .thenThrow(new EntityNotFoundException("Ingredient not found: " + MISSING_INGREDIENT_ID));

        mockMvc.perform(put(PRODUCT_PATH + "/" + PRODUCT_ID).contentType(MediaType.APPLICATION_JSON).content(PRODUCT_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ingredient not found: " + MISSING_INGREDIENT_ID));
    }

    @Test
    @DisplayName("falls back to the status reason phrase when the exception carries no message")
    void entityNotFoundWithoutMessage_fallsBackToReasonPhrase() throws Exception {
        when(productService.getProductById(MISSING_ID)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get(PRODUCT_PATH + "/" + MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("maps DuplicateResourceException to 409 with the exception message")
    void duplicateResource_isMappedToConflict() throws Exception {
        when(userService.registerUser(any(RegistrationRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Email already in use: " + EMAIL));

        mockMvc.perform(post(REGISTER_PATH).contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTRATION))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Email already in use: " + EMAIL));
    }

    @Test
    @DisplayName("returns 201 and does not engage the advice when registration succeeds")
    void successfulRegistration_isNotTouchedByTheAdvice() throws Exception {
        UserResponseDTO created = new UserResponseDTO();

        created.setId(1L);
        created.setEmail(EMAIL);
        created.setRoles(Set.of("ROLE_CUSTOMER"));
        created.setCreatedAt(Instant.now());
        created.setUpdatedAt(Instant.now());

        when(userService.registerUser(any(RegistrationRequestDTO.class))).thenReturn(created);

        mockMvc.perform(post(REGISTER_PATH).contentType(MediaType.APPLICATION_JSON).content(VALID_REGISTRATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("emits exactly one error key and never a stack trace")
    void errorBody_carriesOnlyTheErrorKey() throws Exception {
        when(productService.getProductById(MISSING_ID)).thenThrow(new EntityNotFoundException("Product not found: " + MISSING_ID));

        mockMvc.perform(get(PRODUCT_PATH + "/" + MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.cause").doesNotExist())
                .andExpect(jsonPath("$.suppressed").doesNotExist())
                .andExpect(jsonPath("$.localizedMessage").doesNotExist());
    }

    @Test
    @DisplayName("rejects a malformed registration body with 400 before reaching the service")
    void invalidRegistrationBody_isRejectedWithBadRequest() throws Exception {
        mockMvc.perform(post(REGISTER_PATH).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("does not map IllegalArgumentException to a client error status")
    void illegalArgument_isNotMappedToAClientError() {
        when(productService.getProductById(MISSING_ID)).thenThrow(new IllegalArgumentException("Unknown database code: FRUITS"));

        assertThatThrownBy(() -> mockMvc.perform(get(PRODUCT_PATH + "/" + MISSING_ID)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }
}
