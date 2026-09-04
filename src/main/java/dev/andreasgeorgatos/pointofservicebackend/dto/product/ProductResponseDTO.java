package dev.andreasgeorgatos.pointofservicebackend.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductResponseDTO(
        @NotBlank
        Long id,
        @NotBlank
        @Size(min = 2, max = 120)
        String name,
        @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 8, fraction = 2)
        BigDecimal price
) {
}
