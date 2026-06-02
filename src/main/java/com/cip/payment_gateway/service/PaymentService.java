package com.cip.payment_gateway.service;

import com.cip.payment_gateway.dto.request.PaymentRequest;
import com.cip.payment_gateway.dto.response.PaymentResponse;
import java.util.UUID;

public interface PaymentService {
    // Create a new payment transaction
    PaymentResponse createPayment(PaymentRequest request);

    // Retrieve the status of a transaction
    PaymentResponse getPayment(UUID id);

    // Soft delete a transaction
    PaymentResponse removePayment(String orderId);
}
