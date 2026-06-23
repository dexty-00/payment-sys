package com.PaymentSystem.demo.Controller;


import com.PaymentSystem.demo.Service.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CheckoutController {

    private final StripeService stripeService;

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestParam String email) throws StripeException {
        return ResponseEntity.ok(stripeService.createCheckout(email));
    }

    @GetMapping("/success")
    public String success(@RequestParam String session_id) {
        return "Payment successful! Session: " + session_id;
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "Payment cancelled.";
    }
}