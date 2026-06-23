package com.PaymentSystem.demo.Controller;

import com.PaymentSystem.demo.Entity.CreditBalance;
import com.PaymentSystem.demo.Entity.DTO.CreditSubscriptionRequest;
import com.PaymentSystem.demo.Service.CreditService;
import com.PaymentSystem.demo.Service.SubscriptionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;
    private final SubscriptionManager subscriptionManager;

    @PostMapping("/subscribe")
    public ResponseEntity<CreditBalance> subscribe(@RequestBody CreditSubscriptionRequest request) {
        // Use manager to switch from Stripe to credits
        return ResponseEntity.ok(subscriptionManager.switchToCredits(request.getUserId(), request));
    }

    @PostMapping("/topup")
    public ResponseEntity<CreditBalance> topUp(
            @RequestParam String userId,
            @RequestParam Integer amount) {
        return ResponseEntity.ok(creditService.topUpCredits(userId, amount));
    }

    @GetMapping("/balance")
    public ResponseEntity<CreditBalance> getBalance(@RequestParam String userId) {
        return ResponseEntity.ok(creditService.getBalance(userId));
    }

    @PostMapping("/deduct")
    public ResponseEntity<Boolean> deduct(@RequestParam String userId) {
        return ResponseEntity.ok(creditService.deductCredits(userId));
    }
}