package dev.andreasgeorgatos.pointofservicebackend.services;

import dev.andreasgeorgatos.pointofservicebackend.dto.UserRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.UserResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Role;
import dev.andreasgeorgatos.pointofservicebackend.models.users.User;
import dev.andreasgeorgatos.pointofservicebackend.repository.RoleRepository;
import dev.andreasgeorgatos.pointofservicebackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImplementation userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();

        userRole.setId(1L);
        userRole.setName("ROLE_USER");

        existingUser = new User();

        existingUser.setId(42L);
        existingUser.setEmail("georgatos@andreasgeorgatos.dev");
        existingUser.setPasswordHash("hashed_pw");
        existingUser.setRoles(Set.of(userRole));
        existingUser.setCreatedAt(Instant.now());
        existingUser.setUpdatedAt(Instant.now());
    }

    @Test
    void createUser_hashesPasswordBeforeSaving() {
        UserRequestDTO request = new UserRequestDTO();

        request.setEmail("georgatos@andreasgeorgatos.dev");
        request.setPassword("plainTextPassword123");
        request.setRoleIds(Set.of(1L));

        Role userRole = new Role();

        userRole.setId(1L);
        userRole.setName("ROLE_USER");

        when(userRepository.existsByEmail("georgatos@andreasgeorgatos.dev")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("plainTextPassword123")).thenReturn("FAKE_HASHED_VALUE");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        verify(passwordEncoder).encode("plainTextPassword123");

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getPasswordHash()).isEqualTo("FAKE_HASHED_VALUE");
        assertThat(capturedUser.getPasswordHash()).isNotEqualTo("plainTextPassword123");
    }

    @Test
    void getUserById_whenUserExists_returnsMappedDto() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(existingUser));

        UserResponseDTO result = userService.getUserById(42L);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getEmail()).isEqualTo("georgatos@andreasgeorgatos.dev");
        assertThat(result.getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void getUserById_whenUserMissing_throwsEntityNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found: 99");
    }
}