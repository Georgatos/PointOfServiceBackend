package dev.andreasgeorgatos.pointofservicebackend.models.users;

import dev.andreasgeorgatos.pointofservicebackend.enums.MembershipTier;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "Member", indexes = {
        @Index(name = "idx_member_user_id", columnList = "user_id")
})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String membershipNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private MembershipTier membershipTier;

    @Column(nullable = false)
    private Long pointsBalance;

    private Instant enrolledAt;
    private Instant lastVisitedAt;

    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        if (this.enrolledAt == null) {
            this.enrolledAt = Instant.now();
        }
        if (this.pointsBalance == null) {
            this.pointsBalance = 0L;
        }
    }


}
