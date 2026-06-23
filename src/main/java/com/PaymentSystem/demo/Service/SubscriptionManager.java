package com.PaymentSystem.demo.Service;


import com.PaymentSystem.demo.Entity.CreditBalance;
import com.PaymentSystem.demo.Entity.SubscriptionRecord;
import com.PaymentSystem.demo.Entity.User;
import com.PaymentSystem.demo.Repository.CreditBalanceRepository;
import com.PaymentSystem.demo.Repository.SubscriptionRepository;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class SubscriptionManager {

    private final SubscriptionRepository subscriptionRepository;
    private final CreditBalanceRepository creditBalanceRepository;
    private final StripeService stripeService;
    private final CreditService creditService;
    private final UserService userService;

    @Transactional
    public CreditBalance switchToCredits(Long userId, Integer initialBalance, Integer monthlyCost) {
        User user = userService.getUserById(userId);
        SubscriptionRecord stripeSub = stripeService.getSubscriptionByUserId(user.getId());
        Long nextDeduction = stripeSub.getCurrentPeriodEnd();

        // Deactivate existing credits
        creditBalanceRepository.findByUser_Id(userId)
                .ifPresent(cb -> {
                    cb.setActive(false);
                    creditBalanceRepository.save(cb);
                });

        // Cancel Stripe if active

        subscriptionRepository.findByUser_Id(userId)
                .ifPresent(sub -> {
                    if (sub.isActive()) {
                        try {
                            stripeService.cancelStripeSubscription(sub.getStripeSubscriptionId());
                            sub.setStatus("canceled");
                            subscriptionRepository.save(sub);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to cancel Stripe", e);
                        }
                    }
                });


        return creditService.changeToCreditSubscription(user, initialBalance, monthlyCost,nextDeduction);
    }

    @Transactional
    public String switchToStripe(Long userId) throws StripeException {
        User user = userService.getUserById(userId);

        // Cancel credits if active
        creditBalanceRepository.findByUser_Id(userId)
                .ifPresent(cb -> {
                    cb.setActive(false);
                    creditBalanceRepository.save(cb);
                });

        return stripeService.createCheckout(user.getEmail());
    }

    public boolean isUserSubscribed(Long userId) {
        boolean stripeActive = subscriptionRepository.findByUser_Id(userId)
                .map(SubscriptionRecord::isActive)
                .orElse(false);

        if (stripeActive) return true;

        return creditBalanceRepository.findByUser_Id(userId)
                .map(CreditBalance::getActive)
                .orElse(false);
    }
}