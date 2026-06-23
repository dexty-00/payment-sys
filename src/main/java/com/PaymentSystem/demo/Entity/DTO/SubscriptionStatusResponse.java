package com.PaymentSystem.demo.Entity.DTO;

import lombok.Data;

@Data
public class SubscriptionStatusResponse {
    private boolean active;
    private String status;
    private String plan;
    private Long currentPeriodEnd;
    private boolean cancelAtPeriodEnd;
}