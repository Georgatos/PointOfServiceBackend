package dev.andreasgeorgatos.pointofservicebackend.models.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String city;

    @Column(unique = true, nullable = false)
    private String state;

    @Column(unique = true, nullable = false)
    private String country;

    @Column(unique = true, nullable = false)
    private String zipcode;

    @Column(unique = true, nullable = false)
    private String street;

    @Column(unique = true, nullable = false)
    private String number;

}
