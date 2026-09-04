package dev.andreasgeorgatos.pointofservicebackend.models.items;

import dev.andreasgeorgatos.pointofservicebackend.enums.Allergen;
import dev.andreasgeorgatos.pointofservicebackend.enums.Category;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Data
@Table(name = "Ingredient")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(length = 500)
    private String image;

    @ElementCollection(targetClass = Allergen.class)
    @CollectionTable(
            name = "ingredient_allergens",
            joinColumns = @JoinColumn(name = "ingredient_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen")
    private Set<Allergen> allergens;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerUnit;
}