package com.PaymentSystem.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String stripeCustomerId;

    @Column(nullable = false, unique = true)
    private String stripeSubscriptionId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String status;

    @Column
    private String currentPriceId;

    @Column
    private Long currentPeriodEnd;

    @Column
    private Boolean cancelAtPeriodEnd;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isActive() {
        return "active".equals(status) || "trialing".equals(status);
    }
}