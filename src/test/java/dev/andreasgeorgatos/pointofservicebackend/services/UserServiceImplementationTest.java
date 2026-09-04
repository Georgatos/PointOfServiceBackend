package dev.andreasgeorgatos.pointofservicebackend.services;

import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserRequestEmailDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Role;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Users;
import dev.andreasgeorgatos.pointofservicebackend.repository.RoleRepository;
import dev.andreasgeorgatos.pointofservicebackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("User Service Implementation Test")
class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImplementation userService;

    private Users existingUsers;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();

        userRole.setId(1L);
        userRole.setName("ROLE_USER");

        existingUsers = new Users();

        existingUsers.setId(42L);
        existingUsers.setEmail("georgatos@andreasgeorgatos.dev");
        existingUsers.setPasswordHash("hashed_pw");
        existingUsers.setRoles(Set.of(userRole));
        existingUsers.setCreatedAt(Instant.now());
        existingUsers.setUpdatedAt(Instant.now());
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
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        verify(passwordEncoder).encode("plainTextPassword123");

        ArgumentCaptor<Users> userArgumentCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        Users capturedUsers = userArgumentCaptor.getValue();

        assertThat(capturedUsers.getPasswordHash()).isEqualTo("FAKE_HASHED_VALUE");
        assertThat(capturedUsers.getPasswordHash()).isNotEqualTo("plainTextPassword123");
    }

    @Test
    void getUserById_whenUserExists_returnsMappedDto() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(existingUsers));

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

    @Test
    void updateUser_whenUserMissing_throwsEntityNotFound() {
        UserRequestEmailDTO request = new UserRequestEmailDTO();
        request.setEmail("georgatos@andreasgeorgatos.dev");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserEmail(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found: 99");
    }

}