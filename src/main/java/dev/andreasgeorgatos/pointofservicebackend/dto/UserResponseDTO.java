package dev.andreasgeorgatos.pointofservicebackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
public class UserResponseDTO {

    private Long id;
    private String email;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;

}
