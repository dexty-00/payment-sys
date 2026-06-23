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

    @OneToOne
    @JoinColumn(name = "user_fk", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer balance;

    @Column(nullable = false)
    private Integer monthlyCost;

    @Column
    private Instant lastDeductionDate;

    @Column
    private Instant nextDeductionDate;

    @Column
    private Boolean active;

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
            nextDeductionDate = Instant.now().plusSeconds(30L * 24 * 60 * 60);
        }
    }
}