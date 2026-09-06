package dev.andreasgeorgatos.pointofservicebackend.controllers;

import dev.andreasgeorgatos.pointofservicebackend.dto.user.LoginRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.LoginResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.security.JwtUtility;
import dev.andreasgeorgatos.pointofservicebackend.security.UserPrincipal;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController unit tests")
class AuthControllerTest {

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "s3cr3t-password";
    private static final long USER_ID = 42L;
    private static final String USERNAME = "user@example.com";
    private static final String TOKEN = "header.payload.signature";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtility jwtUtility;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private AuthController authController;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor;

    @Captor
    private ArgumentCaptor<Long> userIdCaptor;

    @Captor
    private ArgumentCaptor<String> subjectCaptor;

    private LoginRequestDTO request;

    @BeforeEach
    void setUp() {
        request = loginRequest(EMAIL, PASSWORD);
    }

    private LoginRequestDTO loginRequest(String email, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();

        dto.setEmail(email);
        dto.setPassword(password);

        return dto;
    }

    private void stubSuccessfulAuthentication() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(USER_ID);
        when(userPrincipal.getUsername()).thenReturn(USERNAME);
        when(jwtUtility.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);
    }

    @Test
    @DisplayName("returns 200 with the token exactly as the factory produced it")
    void login_validCredentials_returnsTokenVerbatim() {
        stubSuccessfulAuthentication();

        ResponseEntity<LoginResponseDTO> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo(TOKEN);
    }

    @Test
    @DisplayName("passes the submitted email and password to the AuthenticationManager")
    void login_passesCredentialsToAuthenticationManager() {
        stubSuccessfulAuthentication();

        authController.login(request);

        verify(authenticationManager).authenticate(authTokenCaptor.capture());

        UsernamePasswordAuthenticationToken captured = authTokenCaptor.getValue();

        assertThat(captured.getPrincipal()).isEqualTo(EMAIL);
        assertThat(captured.getCredentials()).isEqualTo(PASSWORD);
    }

    @Test
    @DisplayName("submits the email verbatim and leaves canonicalisation to the UserDetailsService")
    void login_doesNotNormaliseEmail() {
        stubSuccessfulAuthentication();

        String mixedCaseEmail = "  User@Example.COM  ";

        authController.login(loginRequest(mixedCaseEmail, PASSWORD));

        verify(authenticationManager).authenticate(authTokenCaptor.capture());

        assertThat(authTokenCaptor.getValue().getPrincipal()).isEqualTo(mixedCaseEmail);
    }

    @Test
    @DisplayName("authenticates exactly once per login attempt")
    void login_authenticatesExactlyOnce() {
        stubSuccessfulAuthentication();

        authController.login(request);

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("generates the token from the principal's id and username")
    void login_generatesTokenFromPrincipal() {
        stubSuccessfulAuthentication();

        authController.login(request);

        verify(jwtUtility).generateToken(USER_ID, USERNAME);
    }

    @Test
    @DisplayName("never hands the submitted password to the token factory")
    void login_neverPassesPasswordToTokenFactory() {
        stubSuccessfulAuthentication();

        authController.login(request);

        verify(jwtUtility).generateToken(userIdCaptor.capture(), subjectCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo(USER_ID);
        assertThat(subjectCaptor.getValue()).isEqualTo(USERNAME).isNotEqualTo(PASSWORD);
    }

    @Test
    @DisplayName("propagates BadCredentialsException and issues no token")
    void login_badCredentials_propagatesAndIssuesNoToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

        verifyNoInteractions(jwtUtility);
    }

    @Test
    @DisplayName("propagates DisabledException for a deactivated account and issues no token")
    void login_disabledAccount_propagatesAndIssuesNoToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("User is disabled"));

        assertThatThrownBy(() -> authController.login(request)).isInstanceOf(DisabledException.class);

        verifyNoInteractions(jwtUtility);
    }

    @Test
    @DisplayName("throws ClassCastException when the principal is not a UserPrincipal")
    void login_principalWrongType_throwsClassCastException() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-user-principal");

        assertThatThrownBy(() -> authController.login(request)).isInstanceOf(ClassCastException.class);

        verifyNoInteractions(jwtUtility);
    }

    @Test
    @DisplayName("throws NullPointerException when the authentication carries no principal")
    void login_nullPrincipal_throwsNullPointerException() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        assertThatThrownBy(() -> authController.login(request)).isInstanceOf(NullPointerException.class);

        verifyNoInteractions(jwtUtility);
    }
}
