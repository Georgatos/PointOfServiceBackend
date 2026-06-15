package dev.andreasgeorgatos.pointofservicebackend.models.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String password;
}
