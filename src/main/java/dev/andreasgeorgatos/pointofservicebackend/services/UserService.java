package dev.andreasgeorgatos.pointofservicebackend.services;

import dev.andreasgeorgatos.pointofservicebackend.dto.RegistrationRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.UserRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.UserResponseDTO;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Role;
import jakarta.persistence.EntityNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface UserService {

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long id);

    UserResponseDTO createUser(UserRequestDTO request);

    UserResponseDTO updateUser(Long id, UserRequestDTO request);

    UserResponseDTO registerUser(RegistrationRequestDTO request);

    void deleteUser(Long id);
}
