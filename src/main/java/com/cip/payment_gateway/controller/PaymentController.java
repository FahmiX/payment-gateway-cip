package com.cip.payment_gateway.controller;

// Service
import com.cip.payment_gateway.service.PaymentService;
// Request DTO
import com.cip.payment_gateway.dto.request.PaymentRequest;
// Response DTO
import com.cip.payment_gateway.dto.response.PaymentResponse;
// Other imports
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    // Service injection
    private final PaymentService paymentService;

    // POST /api/payments
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        // Implementation for processing payment
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    // GET /api/payments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String id) {
        // Implementation for retrieving payment
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(response);
    }
}
