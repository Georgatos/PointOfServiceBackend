package dev.andreasgeorgatos.pointofservicebackend.repository;

import dev.andreasgeorgatos.pointofservicebackend.models.items.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);
}
