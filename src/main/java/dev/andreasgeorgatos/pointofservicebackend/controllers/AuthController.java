package dev.andreasgeorgatos.pointofservicebackend.controllers;

import dev.andreasgeorgatos.pointofservicebackend.dto.LoginRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.LoginResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.security.JwtUtility;
import dev.andreasgeorgatos.pointofservicebackend.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v0/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtility jwtUtility;


    public AuthController(AuthenticationManager authenticationManager, JwtUtility jwtUtility) {
        this.authenticationManager = authenticationManager;
        this.jwtUtility = jwtUtility;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String token = jwtUtility.generateToken(principal.getId(), principal.getUsername());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
