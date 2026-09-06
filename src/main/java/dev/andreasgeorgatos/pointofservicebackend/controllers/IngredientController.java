package dev.andreasgeorgatos.pointofservicebackend.controllers;

import dev.andreasgeorgatos.pointofservicebackend.models.items.Ingredient;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v0/ingredient")
public class IngredientController {

    @GetMapping
    @PreAuthorize("hasAuthority('ingredient:ViewAllIngredients')")
    public ResponseEntity<List<Ingredient>> getAllIngredients() {
        // TODO implement this method after the creation of the DTO
        return null;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ingredient:CreateIngredient')")
    public ResponseEntity<Ingredient> createIngredient(@Valid @RequestBody Ingredient ingredient) {
        // TODO implement this method after the creation of the DTO
        return null;
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ingredient:ViewIngredient')")
    public ResponseEntity<Ingredient> getIngredient(@PathVariable long id) {
        // TODO implement this method
        return null;
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ingredient:EditIngredient')")
    public ResponseEntity<Ingredient> editIngredient(@PathVariable long id, @Valid @RequestBody Ingredient ingredient) {
        // TODO implement this method
        return null;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ingredient:DeleteIngredient')")
    public ResponseEntity<Ingredient> deleteIngredient(@PathVariable long id, @Valid @RequestBody Ingredient ingredient) {
        // TODO implement this method
        return null;
    }
}
