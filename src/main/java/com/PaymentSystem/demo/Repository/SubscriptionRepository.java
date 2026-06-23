package com.PaymentSystem.demo.Repository;
import com.PaymentSystem.demo.Entity.SubscriptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionRecord, Long> {
    Optional<SubscriptionRecord> findByStripeSubscriptionId(String stripeSubscriptionId);
    Optional<SubscriptionRecord> findByUserId(String userId);
    Optional<SubscriptionRecord> findByStripeCustomerId(String stripeCustomerId);
}