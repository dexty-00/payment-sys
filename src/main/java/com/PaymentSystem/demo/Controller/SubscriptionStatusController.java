package com.PaymentSystem.demo.Controller;

import com.PaymentSystem.demo.Entity.CreditBalance;
import com.PaymentSystem.demo.Entity.SubscriptionRecord;
import com.PaymentSystem.demo.Service.CreditService;
import com.PaymentSystem.demo.Service.StripeService;
import com.PaymentSystem.demo.Service.SubscriptionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionStatusController {

    private final StripeService stripeService;
    private final CreditService creditService;
    private final SubscriptionManager subscriptionManager;

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestParam Long userId) {
        SubscriptionRecord stripeSub = stripeService.getSubscriptionByUserId(userId);
        if (stripeSub != null && stripeSub.isActive()) {
            return ResponseEntity.ok(Map.of(
                    "subscribed", true,
                    "system", "stripe",
                    "status", stripeSub.getStatus(),
                    "plan", stripeSub.getCurrentPriceId(),
                    "currentPeriodEnd", stripeSub.getCurrentPeriodEnd(),
                    "cancelAtPeriodEnd", stripeSub.getCancelAtPeriodEnd()
            ));
        }

        CreditBalance credit = creditService.getBalance(userId);
        if (credit != null && credit.getActive()) {
            return ResponseEntity.ok(Map.of(
                    "subscribed", true,
                    "system", "credits",
                    "balance", credit.getBalance(),
                    "monthlyCost", credit.getMonthlyCost(),
                    "nextDeductionDate", credit.getNextDeductionDate()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "subscribed", false,
                "status", "none"
        ));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isSubscribed(@RequestParam Long userId) {
        return ResponseEntity.ok(subscriptionManager.isUserSubscribed(userId));
    }
}