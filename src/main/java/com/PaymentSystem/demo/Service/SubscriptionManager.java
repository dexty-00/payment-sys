package com.PaymentSystem.demo.Service;


import com.PaymentSystem.demo.Entity.CreditBalance;
import com.PaymentSystem.demo.Entity.DTO.CreditSubscriptionRequest;
import com.PaymentSystem.demo.Entity.SubscriptionRecord;
import com.PaymentSystem.demo.Repository.CreditBalanceRepository;
import com.PaymentSystem.demo.Repository.SubscriptionRepository;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionManager {

    private final SubscriptionRepository subscriptionRepository;
    private final CreditBalanceRepository creditBalanceRepository;
    private final StripeService stripeService;
    private final CreditService creditService;

    @Transactional
    public String switchToStripe(String userId, String email) throws StripeException {
        // Cancel credits if active
        creditBalanceRepository.findByUserId(userId)
                .ifPresent(cb -> {
                    cb.setActive(false);
                    creditBalanceRepository.save(cb);
                    log.info("Canceled credit subscription for user={}", userId);
                });

        // Return Stripe checkout URL
        return stripeService.createCheckout(userId, email);
    }

    @Transactional
    public CreditBalance switchToCredits(String userId, CreditSubscriptionRequest request) {
        // Cancel Stripe if active
        subscriptionRepository.findByUserId(userId)
                .ifPresent(sub -> {
                    if (sub.isActive()) {
                        try {
                            stripeService.cancelStripeSubscription(sub.getStripeSubscriptionId());
                            sub.setStatus("canceled");
                            subscriptionRepository.save(sub);
                            log.info("Canceled Stripe subscription for user={}", userId);
                        } catch (Exception e) {
                            log.error("Failed to cancel Stripe for user={}", userId, e);
                        }
                    }
                });

        // Deactivate any existing credit subscription
        creditBalanceRepository.findByUserId(userId)
                .ifPresent(cb -> {
                    cb.setActive(false);
                    creditBalanceRepository.save(cb);
                });

        // Create new credit subscription
        return creditService.createCreditSubscription(request);
    }

    @Transactional
    public void cancelCredits(String userId) {
        creditBalanceRepository.findByUserId(userId)
                .ifPresent(cb -> {
                    cb.setActive(false);
                    creditBalanceRepository.save(cb);
                    log.info("Canceled credit subscription for user={}", userId);
                });
    }

    @Transactional
    public void cancelStripe(String userId) {
        subscriptionRepository.findByUserId(userId)
                .ifPresent(sub -> {
                    if (sub.isActive()) {
                        try {
                            stripeService.cancelStripeSubscription(sub.getStripeSubscriptionId());
                            sub.setStatus("canceled");
                            subscriptionRepository.save(sub);
                        } catch (Exception e) {
                            log.error("Failed to cancel Stripe", e);
                        }
                    }
                });
    }

    public boolean isUserSubscribed(String userId) {
        // Check Stripe
        boolean stripeActive = subscriptionRepository.findByUserId(userId)
                .map(SubscriptionRecord::isActive)
                .orElse(false);

        if (stripeActive) return true;

        // Check Credits
        return creditBalanceRepository.findByUserId(userId)
                .map(CreditBalance::getActive)
                .orElse(false);
    }
}