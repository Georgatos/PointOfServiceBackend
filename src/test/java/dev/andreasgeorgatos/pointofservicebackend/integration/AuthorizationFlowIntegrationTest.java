package dev.andreasgeorgatos.pointofservicebackend.integration;

import dev.andreasgeorgatos.pointofservicebackend.models.users.Role;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Users;
import dev.andreasgeorgatos.pointofservicebackend.repository.RoleRepository;
import dev.andreasgeorgatos.pointofservicebackend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Authorization flow end to end")
class AuthorizationFlowIntegrationTest extends AbstractIntegrationTest {

    private static final String PRODUCT_PATH = "/api/v0/product";
    private static final String PRODUCT_BODY = "{\"name\":\"Espresso\",\"description\":\"A double shot.\",\"image\":\"espresso.png\",\"ingredientIds\":[1],\"category\":\"BEVERAGES\",\"price\":2.50}";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String loginAsAdmin() throws Exception {
        Role admin = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

        Users user = new Users();

        String email = uniqueEmail();

        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRoles(Set.of(admin));

        userRepository.save(user);

        return login(email);
    }

    @Test
    @DisplayName("registers a new account and returns 201 with its details")
    void registrationReturnsCreated() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/v0/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("issues a usable token on login")
    void loginIssuesAToken() throws Exception {
        String token = registerAndLogin();

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("canonicalises the email so a differently cased login still works")
    void loginAcceptsADifferentlyCasedEmail() throws Exception {
        String email = uniqueEmail();

        register(email);

        mockMvc.perform(post("/api/v0/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email.toUpperCase(), PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("lets a customer list products")
    void customerCanListProducts() throws Exception {
        mockMvc.perform(get(PRODUCT_PATH).header(HttpHeaders.AUTHORIZATION, bearer(registerAndLogin())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("refuses a customer trying to delete a product")
    void customerCannotDeleteProducts() throws Exception {
        mockMvc.perform(delete(PRODUCT_PATH + "/1").header(HttpHeaders.AUTHORIZATION, bearer(registerAndLogin())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("refuses a customer trying to create a product")
    void customerCannotCreateProducts() throws Exception {
        mockMvc.perform(post(PRODUCT_PATH)
                        .header(HttpHeaders.AUTHORIZATION, bearer(registerAndLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("lets an administrator past the delete permission check")
    void administratorPassesThePermissionCheckOnDelete() throws Exception {
        mockMvc.perform(delete(PRODUCT_PATH + "/999999").header(HttpHeaders.AUTHORIZATION, bearer(loginAsAdmin())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("refuses an unauthenticated request to a protected endpoint")
    void unauthenticatedRequestIsRefused() throws Exception {
        mockMvc.perform(get(PRODUCT_PATH)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("refuses a request carrying a malformed token")
    void malformedTokenIsRefused() throws Exception {
        mockMvc.perform(get(PRODUCT_PATH).header(HttpHeaders.AUTHORIZATION, bearer("not.a.jwt")))
                .andExpect(status().isForbidden());
    }
}
