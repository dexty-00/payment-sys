package com.PaymentSystem.demo.Repository;
import com.PaymentSystem.demo.Entity.SubscriptionRecord;
import com.PaymentSystem.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionRecord, Long> {
    Optional<SubscriptionRecord> findByStripeSubscriptionId(String stripeSubscriptionId);
    Optional<SubscriptionRecord> findByUser(User user);
    Optional<SubscriptionRecord> findByUser_Id(Long userId);
}