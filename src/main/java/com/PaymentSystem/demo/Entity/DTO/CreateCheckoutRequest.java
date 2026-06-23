package com.PaymentSystem.demo.Entity.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
@Data
public class CreateCheckoutRequest {
    @NotBlank
    private String userId;
    private String customerEmail;  // Optional: prefill email
}