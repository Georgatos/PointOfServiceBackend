package dev.andreasgeorgatos.pointofservicebackend.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Tag("integration")
abstract class AbstractIntegrationTest {

    protected static final String PASSWORD = "integrationTestPassword1";

    @Autowired
    private WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @BeforeEach
    void buildMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    protected String uniqueEmail() {
        return "it-" + UUID.randomUUID() + "@example.com";
    }

    protected String credentialsJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    protected void register(String email) throws Exception {
        mockMvc.perform(post("/api/v0/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email, PASSWORD)))
                .andExpect(status().isCreated());
    }

    protected String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/v0/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(body, "$.token");
    }

    protected String registerAndLogin() throws Exception {
        String email = uniqueEmail();

        register(email);

        return login(email);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
