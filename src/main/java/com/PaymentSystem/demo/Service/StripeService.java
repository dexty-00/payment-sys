package com.PaymentSystem.demo.Service;


import com.PaymentSystem.demo.Entity.SubscriptionRecord;
import com.PaymentSystem.demo.Repository.SubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.net.Webhook;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;


@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    private final SubscriptionRepository subscriptionRepository;

    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.price.id}")
    private String defaultPriceId;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ========== CHECKOUT ==========

    public String createCheckout(String userId, String customerEmail) throws StripeException {
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(defaultPriceId)
                                .setQuantity(1L)
                                .build()
                )
                .setSuccessUrl(baseUrl + "/api/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "/api/cancel")
                .setClientReferenceId(userId);

        if (customerEmail != null) {
            paramsBuilder.setCustomerEmail(customerEmail);
        }

        Session session = Session.create(paramsBuilder.build());
        return session.getUrl();
    }

    // ========== WEBHOOKS ==========

    @Transactional
    public void processWebhook(String payload, String sigHeader) throws Exception {
        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        log.info("Webhook received: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutCompleted(event);
                break;
            case "invoice.payment_succeeded":
                handleInvoicePaymentSucceeded(event);
                break;
            case "invoice.payment_failed":
                handleInvoicePaymentFailed(event);
                break;
            case "customer.subscription.updated":
                handleSubscriptionUpdated(event);
                break;
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;
            default:
                log.info("Unhandled event: {}", event.getType());
        }
    }

    @Transactional
    protected void handleCheckoutCompleted(Event event) throws Exception {
        Session session = (Session) deserializeEvent(event);
        if (session == null) return;

        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();
        String userId = session.getClientReferenceId();

        Subscription subscription = Subscription.retrieve(subscriptionId);

        String priceId = null;
        Long currentPeriodEnd = null;

        if (subscription.getItems() != null && !subscription.getItems().getData().isEmpty()) {
            SubscriptionItem item = subscription.getItems().getData().get(0);
            currentPeriodEnd = item.getCurrentPeriodEnd();
            if (item.getPrice() != null) {
                priceId = item.getPrice().getId();
            }
        }

        SubscriptionRecord record = SubscriptionRecord.builder()
                .stripeCustomerId(customerId)
                .stripeSubscriptionId(subscriptionId)
                .userId(userId)
                .status(subscription.getStatus())
                .currentPriceId(priceId)
                .currentPeriodEnd(currentPeriodEnd)
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
                .build();

        subscriptionRepository.save(record);
        log.info("✅ Saved subscription for user={}, status={}", userId, subscription.getStatus());
    }

    @Transactional
    protected void handleSubscriptionUpdated(Event event) {
        Subscription sub = (Subscription) deserializeEvent(event);
        if (sub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(sub.getId())
                .ifPresent(record -> {
                    record.setStatus(sub.getStatus());
                    record.setCancelAtPeriodEnd(sub.getCancelAtPeriodEnd());

                    if (sub.getItems() != null && !sub.getItems().getData().isEmpty()) {
                        SubscriptionItem item = sub.getItems().getData().get(0);
                        record.setCurrentPeriodEnd(item.getCurrentPeriodEnd());
                        if (item.getPrice() != null) {
                            record.setCurrentPriceId(item.getPrice().getId());
                        }
                    }

                    subscriptionRepository.save(record);
                    log.info("📝 Updated subscription {} -> {}", sub.getId(), sub.getStatus());
                });
    }

    @Transactional
    protected void handleSubscriptionDeleted(Event event) {
        Subscription sub = (Subscription) deserializeEvent(event);
        if (sub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(sub.getId())
                .ifPresent(record -> {
                    record.setStatus("canceled");
                    subscriptionRepository.save(record);
                    log.info("❌ Subscription canceled: {}", sub.getId());
                });
    }

    private void handleInvoicePaymentSucceeded(Event event) throws Exception {
        Invoice invoice = (Invoice) deserializeEvent(event);
        if (invoice == null) return;
        log.info("💰 Payment succeeded for subscription={}");
    }

    private void handleInvoicePaymentFailed(Event event) throws Exception {
        Invoice invoice = (Invoice) deserializeEvent(event);
        if (invoice == null) return;
        log.info("⚠️ Payment failed for subscription={}");
    }

    // ========== CANCEL ==========

    public void cancelStripeSubscription(String stripeSubscriptionId) throws Exception {
        Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
        subscription.cancel();
        log.info("Canceled Stripe subscription: {}", stripeSubscriptionId);
    }

    // ========== PORTAL ==========

    public String createCustomerPortal(String customerId) throws StripeException {
        com.stripe.param.billingportal.SessionCreateParams params =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                        .setCustomer(customerId)
                        .build();

        com.stripe.model.billingportal.Session portalSession =
                com.stripe.model.billingportal.Session.create(params);

        return portalSession.getUrl();
    }

    // ========== HELPER ==========

    private com.stripe.model.StripeObject deserializeEvent(Event event) {
        var deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            return deserializer.getObject().get();
        } else {
            try {
                return deserializer.deserializeUnsafe();
            } catch (Exception e) {
                log.error("Failed to deserialize event: {}", event.getType(), e);
                return null;
            }
        }
    }
    public SubscriptionRecord getSubscriptionByUserId(String userId) {
        return subscriptionRepository.findByUserId(userId).orElse(null);
    }
}
