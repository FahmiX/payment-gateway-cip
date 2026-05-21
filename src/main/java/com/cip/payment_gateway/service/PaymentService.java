package com.cip.payment_gateway.service;

// Repository
import com.cip.payment_gateway.repository.TransactionRepository;
// Model
import com.cip.payment_gateway.model.Transactions;
// DTO  
import com.cip.payment_gateway.dto.request.PaymentRequest;
import com.cip.payment_gateway.dto.response.PaymentResponse;
// Enums
import com.cip.payment_gateway.enums.TransactionChannel;
import com.cip.payment_gateway.enums.TransactionStatus;
// Other imports
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    // Repository injection
    private final TransactionRepository transactionRepository;

    /* Services */
    // Process payment
    public PaymentResponse processPayment(PaymentRequest request) {
        //
    }

    // Get payment by ID
    public PaymentResponse getPayment(String id) {
        //
    }
}
