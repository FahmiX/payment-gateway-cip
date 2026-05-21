package com.cip.payment_gateway.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
    // Total fields: 6
    // transactionId, orderId, status, corebankReference, billerReference, message

    private String transactionId;
    private String orderId;
    private String status;
    private String corebankReference;
    private String billerReference;
    private String message;
}
