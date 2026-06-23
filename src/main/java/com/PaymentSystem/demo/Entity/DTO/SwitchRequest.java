package com.PaymentSystem.demo.Entity.DTO;

import lombok.Data;

@Data
public class SwitchRequest {
    private String userId;
    private String targetSystem;  // "stripe" or "credits"
    private String newPriceId;    // For switching to Stripe
    private Integer creditAmount; // For topping up credits
}