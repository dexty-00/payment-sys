package com.PaymentSystem.demo.Controller;
import com.PaymentSystem.demo.Service.PortalService;
import com.PaymentSystem.demo.Service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;

    @PostMapping("/portal")
    public ResponseEntity<String> createPortal(@RequestParam String customerId)
            throws StripeException {
        String portalUrl = portalService.createCustomerPortal(customerId);
        return ResponseEntity.ok(portalUrl);
    }
}