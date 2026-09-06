package dev.andreasgeorgatos.pointofservicebackend.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Error responses through the real stack")
class ErrorResponseIntegrationTest extends AbstractIntegrationTest {

    private static final String REGISTER_PATH = "/api/v0/users/register";

    @Test
    @DisplayName("maps a duplicate registration to 409 through the registered advice")
    void duplicateRegistrationReturnsConflict() throws Exception {
        String email = uniqueEmail();

        register(email);

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email, PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("treats a differently cased duplicate as the same account")
    void duplicateRegistrationIsCaseInsensitive() throws Exception {
        String email = uniqueEmail();

        register(email);

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email.toUpperCase(), PASSWORD)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("maps a missing product to 404 through the registered advice")
    void missingProductReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v0/product/999999").header(HttpHeaders.AUTHORIZATION, bearer(registerAndLogin())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("rejects a malformed registration body with 400")
    void invalidRegistrationBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson("not-an-email", "short")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("never leaks a stack trace in an error body")
    void errorBodyCarriesNoStackTrace() throws Exception {
        mockMvc.perform(get("/api/v0/product/999999").header(HttpHeaders.AUTHORIZATION, bearer(registerAndLogin())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.cause").doesNotExist());
    }
}
