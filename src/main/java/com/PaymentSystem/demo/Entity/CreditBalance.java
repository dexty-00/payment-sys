package com.PaymentSystem.demo.Entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "credit_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private Integer balance;  // Credits remaining

    @Column(nullable = false)
    private Integer monthlyCost;  // How many credits deducted per month

    @Column
    private Instant lastDeductionDate;

    @Column
    private Instant nextDeductionDate;

    @Column
    private Boolean active;  // Is credit subscription currently active?

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

    public boolean hasEnoughCredits() {
        return balance >= monthlyCost;
    }

    public void deductMonthlyCost() {
        if (hasEnoughCredits()) {
            balance -= monthlyCost;
            lastDeductionDate = Instant.now();
            nextDeductionDate = Instant.now().plusSeconds(30 * 24 * 60 * 60); // ~30 days
        }
    }
}