package dev.andreasgeorgatos.pointofservicebackend.controllers;

import dev.andreasgeorgatos.pointofservicebackend.dto.user.RegistrationRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserRequestEmailDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v0/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('user:viewAll')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('user:readAny') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('user:createUser')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegistrationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('user:updateAnyUserEmail') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDTO> updateUserEmail(@PathVariable Long id, @Valid @RequestBody UserRequestEmailDTO userRequestEmailDTO) {
        return ResponseEntity.ok(userService.updateUserEmail(id, userRequestEmailDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('user:deleteAnyUser') or #id == authentication.principal.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
