package dev.andreasgeorgatos.pointofservicebackend.services;

import dev.andreasgeorgatos.pointofservicebackend.models.items.Ingredient;

import java.util.List;

public interface IngredientService {

    List<Ingredient> getAllIngredients();

    Ingredient getIngredient();

    Ingredient createIngredient(Ingredient ingredient);

    Ingredient updateIngredient(Long id, Ingredient ingredient);

    void deleteIngredient(Long id);


}
