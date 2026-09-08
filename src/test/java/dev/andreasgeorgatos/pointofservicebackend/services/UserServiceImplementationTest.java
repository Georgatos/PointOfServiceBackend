package dev.andreasgeorgatos.pointofservicebackend.services;

import dev.andreasgeorgatos.pointofservicebackend.dto.user.RegistrationRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserRequestEmailDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.exceptions.DuplicateResourceException;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Role;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Users;
import dev.andreasgeorgatos.pointofservicebackend.repository.RoleRepository;
import dev.andreasgeorgatos.pointofservicebackend.repository.UserRepository;
import dev.andreasgeorgatos.pointofservicebackend.services.user.UserServiceImplementation;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImplementation unit tests")
class UserServiceImplementationTest {

    private static final long USER_ID = 42L;
    private static final long OTHER_USER_ID = 43L;
    private static final long MISSING_USER_ID = 99L;
    private static final long ROLE_ID = 1L;
    private static final long DEFAULT_ROLE_ID = 2L;
    private static final long MISSING_ROLE_ID = 77L;

    private static final String ROLE_NAME = "ROLE_USER";
    private static final String DEFAULT_ROLE_NAME = "ROLE_CUSTOMER";

    private static final String EMAIL = "georgatos@andreasgeorgatos.dev";
    private static final String OTHER_EMAIL = "someone.else@andreasgeorgatos.dev";
    private static final String RAW_PASSWORD = "plainTextPassword123";
    private static final String HASHED_PASSWORD = "FAKE_HASHED_VALUE";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<Users> userCaptor;

    private UserServiceImplementation userService;

    private Role userRole;
    private Role defaultRole;
    private Users existingUser;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImplementation(userRepository, roleRepository, passwordEncoder, DEFAULT_ROLE_NAME);

        userRole = new Role();
        userRole.setId(ROLE_ID);
        userRole.setName(ROLE_NAME);

        defaultRole = new Role();
        defaultRole.setId(DEFAULT_ROLE_ID);
        defaultRole.setName(DEFAULT_ROLE_NAME);

        existingUser = new Users();
        existingUser.setId(USER_ID);
        existingUser.setEmail(EMAIL);
        existingUser.setPasswordHash("hashed_pw");
        existingUser.setRoles(Set.of(userRole));
        existingUser.setCreatedAt(Instant.now());
        existingUser.setUpdatedAt(Instant.now());
    }

    private UserRequestDTO userRequest(String email, Set<Long> roleIds) {
        UserRequestDTO request = new UserRequestDTO();

        request.setEmail(email);
        request.setPassword(RAW_PASSWORD);
        request.setRoleIds(roleIds);

        return request;
    }

    private RegistrationRequestDTO registrationRequest(String email) {
        RegistrationRequestDTO request = new RegistrationRequestDTO();

        request.setEmail(email);
        request.setPassword(RAW_PASSWORD);

        return request;
    }

    private UserRequestEmailDTO emailRequest(String email) {
        UserRequestEmailDTO request = new UserRequestEmailDTO();

        request.setEmail(email);

        return request;
    }

    private void returnTheSavedUser() {
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("createUser stores the encoded password and never the plaintext one")
    void createUser_hashesPasswordBeforeSaving() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        returnTheSavedUser();

        userService.createUser(userRequest(EMAIL, Set.of(ROLE_ID)));

        verify(passwordEncoder).encode(RAW_PASSWORD);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(HASHED_PASSWORD).isNotEqualTo(RAW_PASSWORD);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "  georgatos@andreasgeorgatos.dev  ",
            "GEORGATOS@ANDREASGEORGATOS.DEV",
            "Georgatos@AndreasGeorgatos.Dev",
            "\tGEORGATOS@AndreasGeorgatos.DEV\n"
    })
    @DisplayName("createUser canonicalises the email before both the duplicate check and the save")
    void createUser_normalisesEmail(String submittedEmail) {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        returnTheSavedUser();

        UserResponseDTO response = userService.createUser(userRequest(submittedEmail, null));

        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("createUser rejects an address already in use and writes nothing")
    void createUser_duplicateEmail_throwsDuplicateResource() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest(EMAIL, Set.of(ROLE_ID))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(EMAIL);

        verify(userRepository, never()).save(any(Users.class));
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(roleRepository);
    }

    @Test
    @DisplayName("createUser resolves every requested role id to a managed role")
    void createUser_resolvesEveryRequestedRole() {
        Role secondRole = new Role();
        secondRole.setId(DEFAULT_ROLE_ID);
        secondRole.setName(DEFAULT_ROLE_NAME);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(userRole));
        when(roleRepository.findById(DEFAULT_ROLE_ID)).thenReturn(Optional.of(secondRole));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        returnTheSavedUser();

        UserResponseDTO response = userService.createUser(userRequest(EMAIL, Set.of(ROLE_ID, DEFAULT_ROLE_ID)));

        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getRoles()).containsExactlyInAnyOrder(userRole, secondRole);
        assertThat(response.getRoles()).containsExactlyInAnyOrder(ROLE_NAME, DEFAULT_ROLE_NAME);
    }

    @Test
    @DisplayName("createUser rejects an unknown role id and writes nothing")
    void createUser_unknownRoleId_throwsEntityNotFound() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findById(MISSING_ROLE_ID)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);

        assertThatThrownBy(() -> userService.createUser(userRequest(EMAIL, Set.of(MISSING_ROLE_ID))))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.valueOf(MISSING_ROLE_ID));

        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    @DisplayName("createUser tolerates a null role set and saves an unprivileged account")
    void createUser_nullRoleIds_savesUserWithNoRoles() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        returnTheSavedUser();

        UserResponseDTO response = userService.createUser(userRequest(EMAIL, null));

        verify(userRepository).save(userCaptor.capture());
        verifyNoInteractions(roleRepository);

        assertThat(userCaptor.getValue().getRoles()).isEmpty();
        assertThat(response.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("registerUser grants exactly the configured default role")
    void registerUser_assignsConfiguredDefaultRole() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByName(DEFAULT_ROLE_NAME)).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        returnTheSavedUser();

        UserResponseDTO response = userService.registerUser(registrationRequest(EMAIL));

        verify(roleRepository).findByName(DEFAULT_ROLE_NAME);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getRoles()).containsExactly(defaultRole);
        assertThat(response.getRoles()).containsExactly(DEFAULT_ROLE_NAME);
    }

    @Test
    @DisplayName("registerUser stores the encoded password and never the plaintext one")
    void registerUser_hashesPasswordBeforeSaving() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByName(DEFAULT_ROLE_NAME)).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        returnTheSavedUser();

        userService.registerUser(registrationRequest(EMAIL));

        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(HASHED_PASSWORD).isNotEqualTo(RAW_PASSWORD);
    }

    @Test
    @DisplayName("registerUser canonicalises the email before both the duplicate check and the save")
    void registerUser_normalisesEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByName(DEFAULT_ROLE_NAME)).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        returnTheSavedUser();

        userService.registerUser(registrationRequest("  GEORGATOS@AndreasGeorgatos.DEV  "));

        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("registerUser rejects an address already in use and writes nothing")
    void registerUser_duplicateEmail_throwsDuplicateResource() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(registrationRequest(EMAIL)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(EMAIL);

        verify(userRepository, never()).save(any(Users.class));
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(roleRepository);
    }

    @Test
    @DisplayName("registerUser fails loudly when the configured default role is not seeded")
    void registerUser_defaultRoleMissing_throwsEntityNotFound() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(roleRepository.findByName(DEFAULT_ROLE_NAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.registerUser(registrationRequest(EMAIL)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(DEFAULT_ROLE_NAME);

        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    @DisplayName("updateUserEmail moves the account to the new address")
    void updateUserEmail_movesAccountToNewAddress() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(OTHER_EMAIL)).thenReturn(false);
        returnTheSavedUser();

        UserResponseDTO response = userService.updateUserEmail(USER_ID, emailRequest("  Someone.Else@AndreasGeorgatos.DEV  "));

        verify(userRepository).existsByEmail(OTHER_EMAIL);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(OTHER_EMAIL);
        assertThat(response.getEmail()).isEqualTo(OTHER_EMAIL);
    }

    @Test
    @DisplayName("updateUserEmail rejects an address held by another account")
    void updateUserEmail_addressTakenByAnother_throwsDuplicateResource() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(OTHER_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUserEmail(USER_ID, emailRequest(OTHER_EMAIL)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(OTHER_EMAIL);

        verify(userRepository, never()).save(any(Users.class));
        assertThat(existingUser.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("updateUserEmail skips the duplicate check when the address only differs in case")
    void updateUserEmail_sameAddressDifferentCase_skipsDuplicateCheck() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        returnTheSavedUser();

        UserResponseDTO response = userService.updateUserEmail(USER_ID, emailRequest("GEORGATOS@AndreasGeorgatos.DEV"));

        verify(userRepository, never()).existsByEmail(any());

        assertThat(response.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("updateUserEmail rejects an unknown user")
    void updateUserEmail_missingUser_throwsEntityNotFound() {
        when(userRepository.findById(MISSING_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserEmail(MISSING_USER_ID, emailRequest(EMAIL)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found: " + MISSING_USER_ID);

        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    @DisplayName("getUserById maps the stored user onto the response DTO")
    void getUserById_whenUserExists_returnsMappedDto() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        UserResponseDTO response = userService.getUserById(USER_ID);

        assertThat(response.getId()).isEqualTo(USER_ID);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getRoles()).containsExactly(ROLE_NAME);
        assertThat(response.getCreatedAt()).isEqualTo(existingUser.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(existingUser.getUpdatedAt());
    }

    @Test
    @DisplayName("getUserById returns an empty role set rather than null for a user with no roles")
    void getUserById_userWithoutRoles_returnsEmptyRoleSet() {
        existingUser.setRoles(null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        assertThat(userService.getUserById(USER_ID).getRoles()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("getUserById rejects an unknown user")
    void getUserById_whenUserMissing_throwsEntityNotFound() {
        when(userRepository.findById(MISSING_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(MISSING_USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found: " + MISSING_USER_ID);
    }

    @Test
    @DisplayName("getAllUsers maps every stored user")
    void getAllUsers_mapsEveryUser() {
        Users otherUser = new Users();
        otherUser.setId(OTHER_USER_ID);
        otherUser.setEmail(OTHER_EMAIL);
        otherUser.setRoles(Set.of());

        when(userRepository.findAll()).thenReturn(List.of(existingUser, otherUser));

        assertThat(userService.getAllUsers())
                .extracting(UserResponseDTO::getEmail)
                .containsExactly(EMAIL, OTHER_EMAIL);
    }

    @Test
    @DisplayName("getAllUsers returns an empty list when no users exist")
    void getAllUsers_noUsers_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertThat(userService.getAllUsers()).isEmpty();
    }

    @Test
    @DisplayName("deleteUser removes an existing account by id")
    void deleteUser_existingId_deletesById() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        userService.deleteUser(USER_ID);

        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    @DisplayName("deleteUser rejects an unknown user and deletes nothing")
    void deleteUser_missingId_throwsEntityNotFoundAndDeletesNothing() {
        when(userRepository.existsById(MISSING_USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(MISSING_USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found: " + MISSING_USER_ID);

        verify(userRepository, never()).deleteById(any());
    }
}
