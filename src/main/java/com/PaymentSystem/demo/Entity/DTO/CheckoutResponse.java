package com.PaymentSystem.demo.Entity.DTO;
import lombok.Data;

@Data
public class CheckoutResponse {
    private String checkoutUrl;
    private String sessionId;


}
