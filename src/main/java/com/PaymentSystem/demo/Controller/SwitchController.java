package com.PaymentSystem.demo.Controller;

import com.PaymentSystem.demo.Entity.CreditBalance;
import com.PaymentSystem.demo.Entity.DTO.CreditSubscriptionRequest;
import com.PaymentSystem.demo.Entity.DTO.SwitchRequest;
import com.PaymentSystem.demo.Service.CreditService;
import com.PaymentSystem.demo.Service.StripeService;
import com.PaymentSystem.demo.Service.SubscriptionManager;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/switch")
@RequiredArgsConstructor
public class SwitchController {

    private final SubscriptionManager subscriptionManager;

    @PostMapping("/to-stripe")
    public ResponseEntity<String> switchToStripe(@RequestParam Long userId) throws StripeException {
        String checkoutUrl = subscriptionManager.switchToStripe(userId);
        return ResponseEntity.ok(checkoutUrl);
    }

    @PostMapping("/to-credits")
    public ResponseEntity<?> switchToCredits(@RequestParam Long userId) {
        var result = subscriptionManager.switchToCredits(userId, 2000, 800);
        return ResponseEntity.ok(result);
    }
}