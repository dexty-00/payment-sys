package com.PaymentSystem.demo.Entity.DTO;

import lombok.Data;

@Data
public class CreditSubscriptionRequest {
    private Long userId;
    private Integer initialBalance;  // Credits to start with
    private Integer monthlyCost;     // Credits deducted per month
}