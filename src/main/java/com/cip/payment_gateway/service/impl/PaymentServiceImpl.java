package com.cip.payment_gateway.service.impl;

// Service
import com.cip.payment_gateway.service.PaymentService;
// Repository
import com.cip.payment_gateway.repository.TransactionRepository;
// Model
import com.cip.payment_gateway.model.Transactions;
// DTO  
import com.cip.payment_gateway.dto.request.PaymentRequest;
import com.cip.payment_gateway.dto.response.PaymentResponse;
import com.cip.payment_gateway.dto.request.BillerRequest;
import com.cip.payment_gateway.dto.request.CoreBankRequest;
import com.cip.payment_gateway.dto.response.BillerResponse;
import com.cip.payment_gateway.dto.response.CoreBankResponse;
// Enums
import com.cip.payment_gateway.enums.TransactionChannel;
import com.cip.payment_gateway.enums.TransactionStatus;
import com.cip.payment_gateway.exception.DuplicateOrderException;
import com.cip.payment_gateway.exception.ResourceNotFoundException;
// Feign Clients
import com.cip.payment_gateway.client.CoreBankFeignClient;
import com.cip.payment_gateway.client.BillerFeignClient;
// Other imports
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import java.util.Optional;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    // Repository
    private final TransactionRepository transactionRepository;

    // Feign Client
    private final CoreBankFeignClient coreBankClient;
    private final BillerFeignClient billerClient;

    /* Services */
    // Create payment
    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Log request data
        log.info("Creating payment for orderId: {}", request.getOrderId());

        // Validate Channel
        TransactionChannel.from(request.getChannel());

        // Validate Duplicate Order ID
        Optional<Transactions> transaction = transactionRepository.findByOrderId(request.getOrderId());
        if (transaction.isPresent()) {
            log.warn("Payment already exists for orderId: {}", request.getOrderId());

            throw new DuplicateOrderException("Payment already exists for orderId: " + request.getOrderId());
        }

        // Save pending transaction to database
        Transactions pendingTransaction = Transactions.builder()
                .orderId(request.getOrderId())
                .channel(TransactionChannel.valueOf(request.getChannel()))
                .amount(request.getAmount())
                .account(request.getAccount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status(TransactionStatus.PENDING)
                .build();

        transactionRepository.save(pendingTransaction);

        // Call CoreBank API
        CoreBankResponse coreBankResponse = coreBankClient.debit(CoreBankRequest.builder()
                .account(request.getAccount())
                .amount(request.getAmount())
                .build());

        log.info("CoreBank response for orderId {}: {}", request.getOrderId(), coreBankResponse);

        // Validate CoreBank
        if ("FAILED".equalsIgnoreCase(coreBankResponse.getStatus())) {
            pendingTransaction.setStatus(TransactionStatus.FAILED);

            transactionRepository.save(pendingTransaction);

            log.error("Insufficient balance orderId={}", request.getOrderId());

            return PaymentResponse.builder()
                    .transactionId(pendingTransaction.getId().toString())
                    .orderId(pendingTransaction.getOrderId())
                    .status("FAILED")
                    .message("Insufficient balance")
                    .build();
        }

        // Call Biller API
        BillerResponse billerResponse = billerClient.pay(BillerRequest.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .build());

        log.info("Biller response status={}", billerResponse.getStatus());

        // Validate Biller
        if ("FAILED".equalsIgnoreCase(billerResponse.getStatus())) {
            pendingTransaction.setStatus(TransactionStatus.FAILED);

            transactionRepository.save(pendingTransaction);

            log.error("Biller payment failed, orderId={}", request.getOrderId());

            return PaymentResponse.builder()
                    .transactionId(
                            pendingTransaction.getId().toString())
                    .orderId(pendingTransaction.getOrderId())
                    .status("FAILED")
                    .message("Biller payment failed")
                    .build();
        }

        // Update transaction status to SUCCESS
        pendingTransaction.setStatus(TransactionStatus.SUCCESS);

        pendingTransaction.setCorebankReference(coreBankResponse.getCoreBankReference());

        pendingTransaction.setBillerReference(billerResponse.getBillerReference());

        transactionRepository.save(pendingTransaction);

        log.info("Transaction Success, orderId={}", request.getOrderId());

        // Return response
        PaymentResponse response = PaymentResponse.builder()
                .transactionId(pendingTransaction.getId().toString())
                .orderId(pendingTransaction.getOrderId())
                .status(pendingTransaction.getStatus().name())
                .corebankReference(pendingTransaction.getCorebankReference())
                .billerReference(pendingTransaction.getBillerReference())
                .message("Payment successfully")
                .build();

        return response;
    }

    // Get payment by ID
    @Override
    public PaymentResponse getPayment(UUID id) {
        Transactions transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Payment not found with ID: {}", id);
                    return new ResourceNotFoundException(
                            "Payment not found with ID: " + id);
                });

        log.info("Payment found with ID: {}", id);

        return PaymentResponse.builder()
                .transactionId(transaction.getId().toString())
                .orderId(transaction.getOrderId())
                .status(transaction.getStatus().name())
                .corebankReference(transaction.getCorebankReference())
                .billerReference(transaction.getBillerReference())
                .message("Payment found")
                .build();
    }

    // Delete payment (soft delete)
    @Override
    @Transactional
    public PaymentResponse removePayment(String orderId) {
        // Validasi orderId is founded or not founded
        Transactions transaction = transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment not found with orderId: {}", orderId);
                    return new ResourceNotFoundException(
                            "Payment not found with orderId: " + orderId);
                });

        // Log Found Transaction
        log.info("Payment found with orderId: {}", orderId);

        // If orderId is founded, set status to REMOVED
        transaction.setStatus(TransactionStatus.REMOVED);

        // Save Transaction to database
        transactionRepository.save(transaction);

        log.info("Payment removed with orderId: {}", orderId);

        return PaymentResponse.builder()
                .transactionId(transaction.getId().toString())
                .orderId(transaction.getOrderId())
                .status(transaction.getStatus().name())
                .corebankReference(transaction.getCorebankReference())
                .billerReference(transaction.getBillerReference())
                .message("Payment with orderId " + orderId + " has been removed")
                .build();
    }
}
