package com.PaymentSystem.demo.Controller;


import com.PaymentSystem.demo.Service.StripeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final StripeService stripeService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        String sigHeader = request.getHeader("Stripe-Signature");

        if (sigHeader == null) {
            log.error("Missing Stripe-Signature header");
            return ResponseEntity.badRequest().body("Missing signature");
        }

        // Read RAW body — this is critical!
        String payload;
        try (BufferedReader reader = request.getReader()) {
            payload = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("Failed to read body", e);
            return ResponseEntity.badRequest().body("Bad body");
        }

        try {
            stripeService.processWebhook(payload, sigHeader);
            return ResponseEntity.ok("Received");
        } catch (Exception e) {
            log.error("Webhook failed", e);
            // Return 400 for signature errors (Stripe will retry)
            // Return 200 for processing errors (don't retry forever)
            if (e.getMessage().contains("signature")) {
                return ResponseEntity.badRequest().body("Invalid signature");
            }
            return ResponseEntity.ok("Processed with error");
        }
    }
}
