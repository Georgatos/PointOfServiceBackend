package dev.andreasgeorgatos.pointofservicebackend.models.users;

import dev.andreasgeorgatos.pointofservicebackend.enums.PointsTransactionType;
import dev.andreasgeorgatos.pointofservicebackend.models.orders.Order;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(
        name = "points_transactions",
        indexes = {
                @Index(name = "idx_points_tx_member_id", columnList = "member_id"),
                @Index(name = "idx_points_tx_order_id", columnList = "order_id")
        }
)
public class PointsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long pointsDelta;

    @Column(nullable = false)
    private Long balanceAfter;

    @Enumerated(EnumType.STRING)
    private PointsTransactionType type;

    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
