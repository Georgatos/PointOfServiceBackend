package dev.andreasgeorgatos.pointofservicebackend.repository;

import dev.andreasgeorgatos.pointofservicebackend.models.items.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}
