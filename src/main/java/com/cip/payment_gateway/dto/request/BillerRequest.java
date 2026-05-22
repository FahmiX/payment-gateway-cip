package com.cip.payment_gateway.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillerRequest {
    // Total fields: 3
    // orderId, amount, paymentMethod

    private String orderId;
    private BigDecimal amount;
    private String paymentMethod;
}
