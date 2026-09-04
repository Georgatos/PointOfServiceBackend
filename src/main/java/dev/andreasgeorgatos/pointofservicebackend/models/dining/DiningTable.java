package dev.andreasgeorgatos.pointofservicebackend.models.dining;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "DiningTable")
public class DiningTable {

    @Id
    private Long id;

    @Column(name = "table_number", nullable = false, unique = true)
    private int tableNumber;

    @Column(name = "seat_count", nullable = false)
    private int seatCount;

    @Column(nullable = false)
    private boolean active = true;
}
