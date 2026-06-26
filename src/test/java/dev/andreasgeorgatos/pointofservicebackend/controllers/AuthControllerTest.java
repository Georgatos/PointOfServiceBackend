package dev.andreasgeorgatos.pointofservicebackend.controllers;

import dev.andreasgeorgatos.pointofservicebackend.dto.LoginRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.LoginResponseDTO;
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

    private LoginRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
    }

    @Test
    @DisplayName("returns 200 with a JWT when credentials are valid")
    void login_validCredentials_returnsToken() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(USER_ID);
        when(userPrincipal.getUsername()).thenReturn(USERNAME);
        when(jwtUtility.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);

        ResponseEntity<LoginResponseDTO> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo(TOKEN);
    }

    @Test
    @DisplayName("passes the submitted email and password to the AuthenticationManager")
    void login_passesCredentialsToAuthenticationManager() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(USER_ID);
        when(userPrincipal.getUsername()).thenReturn(USERNAME);
        when(jwtUtility.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);

        authController.login(request);

        verify(authenticationManager).authenticate(authTokenCaptor.capture());

        UsernamePasswordAuthenticationToken captured = authTokenCaptor.getValue();

        assertThat(captured.getPrincipal()).isEqualTo(EMAIL);
        assertThat(captured.getCredentials()).isEqualTo(PASSWORD);
    }

    @Test
    @DisplayName("generates the token from the principal's id and username")
    void login_generatesTokenFromPrincipal() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(USER_ID);
        when(userPrincipal.getUsername()).thenReturn(USERNAME);
        when(jwtUtility.generateToken(USER_ID, USERNAME)).thenReturn(TOKEN);

        authController.login(request);

        verify(jwtUtility).generateToken(USER_ID, USERNAME);
    }

    @Test
    @DisplayName("propagates BadCredentialsException and never issues a token")
    void login_badCredentials_propagatesAndDoesNotGenerateToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authController.login(request)).isInstanceOf(BadCredentialsException.class).hasMessage("Bad credentials");

        verifyNoInteractions(jwtUtility);
    }

    @Test
    @DisplayName("does not generate a token when authentication fails")
    void login_authenticationFails_noTokenGenerated() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authController.login(request)).isInstanceOf(BadCredentialsException.class);

        verify(jwtUtility, never()).generateToken(any(Long.class), any(String.class));
    }

    @Test
    @DisplayName("throws ClassCastException when the principal is not a UserPrincipal")
    void login_principalWrongType_throwsClassCastException() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-user-principal");

        assertThatThrownBy(() -> authController.login(request)).isInstanceOf(ClassCastException.class);
    }
}