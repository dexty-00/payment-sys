package com.PaymentSystem.demo.Service;

import com.stripe.exception.StripeException;
import com.stripe.param.billingportal.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalService {
    public String createCustomerPortal(String customerId) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomer(customerId)
                .build();

        com.stripe.model.billingportal.Session portalSession =
                com.stripe.model.billingportal.Session.create(params);

        return portalSession.getUrl();
    }
}
