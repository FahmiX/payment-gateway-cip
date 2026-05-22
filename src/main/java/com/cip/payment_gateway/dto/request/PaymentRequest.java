package com.cip.payment_gateway.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {
    // Total fields: 6
    // orderId, channel, amount, account, currency, paymentMethod

    private String orderId;
    private String channel;
    private BigDecimal amount;
    private String account;
    private String currency;
    private String paymentMethod;
}
