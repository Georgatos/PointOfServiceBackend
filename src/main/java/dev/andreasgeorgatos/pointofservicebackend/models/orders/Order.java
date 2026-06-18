package dev.andreasgeorgatos.pointofservicebackend.models.orders;

import dev.andreasgeorgatos.pointofservicebackend.models.items.Product;
import dev.andreasgeorgatos.pointofservicebackend.models.users.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@Table(name = "order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", cascade =  CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
