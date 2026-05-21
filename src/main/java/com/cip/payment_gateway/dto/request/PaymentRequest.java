package com.cip.payment_gateway.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    // Total fields: 6
    // orderId, channel, amount, account, currency, paymentMethod

    private String orderId;
    private String channel;
    private String amount;
    private String account;
    private String currency;
    private String paymentMethod;
}
