package dev.andreasgeorgatos.pointofservicebackend.models.users;

import dev.andreasgeorgatos.pointofservicebackend.enums.MembershipTier;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "Member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String membershipNumber;

    @Enumerated(EnumType.STRING)
    private MembershipTier membershipTier;

    @Column(nullable = false)
    private Long pointsBalance = 0;

    private Instant enrolledAt;
    private Instant lastVisitedAt;

    @Version
    private Long version;

}
