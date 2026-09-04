package dev.andreasgeorgatos.pointofservicebackend.services;

import dev.andreasgeorgatos.pointofservicebackend.dto.user.RegistrationRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserRequestEmailDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Role;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Users;
import dev.andreasgeorgatos.pointofservicebackend.repository.RoleRepository;
import dev.andreasgeorgatos.pointofservicebackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultRoleName;

    public UserServiceImplementation(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                                     @Value("${app.registration.default-role}") String defaultRoleName) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultRoleName = defaultRoleName;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        Users users = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        return toResponse(users);
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {

        if (userRepository.existsByEmail(Users.normalizeEmail(request.getEmail()))) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        Users users = new Users();
        users.setEmail(request.getEmail());
        users.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        users.setRoles(resolveRoles(request.getRoleIds()));

        return toResponse(userRepository.save(users));
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserEmail(Long id, UserRequestEmailDTO request) {
        Users users = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        String newEmail = Users.normalizeEmail(request.getEmail());

        if (!newEmail.equals(users.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }
        users.setEmail(newEmail);

        return toResponse(userRepository.save(users));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found: " + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponseDTO registerUser(RegistrationRequestDTO request) {
        if (userRepository.existsByEmail(Users.normalizeEmail(request.getEmail()))) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        Role defaultRole = roleRepository.findByName(defaultRoleName).orElseThrow(() -> new EntityNotFoundException("Default role not found: " + defaultRoleName));

        Users users = new Users();
        users.setEmail(request.getEmail());
        users.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        users.setRoles(Set.of(defaultRole));

        return toResponse(userRepository.save(users));
    }

    private UserResponseDTO toResponse(Users users) {
        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(users.getId());
        dto.setEmail(users.getEmail());
        dto.setRoles(users.getRoles() == null ? Set.of() : users.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        dto.setCreatedAt(users.getCreatedAt());
        dto.setUpdatedAt(users.getUpdatedAt());

        return dto;
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        Set<Role> roles = new HashSet<>();

        if (roleIds == null) {
            return roles;
        }

        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));
            roles.add(role);
        }
        return roles;
    }

}
