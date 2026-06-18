package dev.andreasgeorgatos.pointofservicebackend.models.users;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "PersonalInformation")
public class PersonalInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String firstName;

    @Column(unique = true, nullable = false)
    private String lastName;

    @JoinColumn(unique = true, nullable = false)
    @OneToOne(mappedBy = "personalInformation", cascade = CascadeType.ALL)
    private Address address;
}
