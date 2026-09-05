package dev.andreasgeorgatos.pointofservicebackend.dto.product;

import dev.andreasgeorgatos.pointofservicebackend.enums.Category;
import dev.andreasgeorgatos.pointofservicebackend.models.items.Ingredient;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record ProductCreateRequest(@NotBlank
                                   @Size(min = 2, max = 120)
                                   String name,
                                   @NotBlank
                                   @Size(min = 2, max = 1000)
                                   String description,
                                   @NotBlank
                                   @Size(min = 2, max = 500)
                                   String image,
                                   @NotEmpty
                                   @Size(max = 50, message = "A product cannot have more than 50 ingredients.")
                                   Set<Long> ingredientIds,
                                   @NotNull
                                   Category category,
                                   @NotNull
                                   @DecimalMin(value = "0.00", inclusive = false)
                                   @Digits(integer = 8, fraction = 2)
                                   BigDecimal price) {
}