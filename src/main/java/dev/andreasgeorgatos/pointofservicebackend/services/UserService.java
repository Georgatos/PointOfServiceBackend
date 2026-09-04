package dev.andreasgeorgatos.pointofservicebackend.services;

import dev.andreasgeorgatos.pointofservicebackend.dto.user.RegistrationRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserRequestDTO;
import dev.andreasgeorgatos.pointofservicebackend.dto.user.UserResponseDTO;

import java.util.List;

public interface UserService {

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long id);

    UserResponseDTO createUser(UserRequestDTO request);

    UserResponseDTO updateUser(Long id, UserRequestDTO request);

    UserResponseDTO registerUser(RegistrationRequestDTO request);

    void deleteUser(Long id);
}
