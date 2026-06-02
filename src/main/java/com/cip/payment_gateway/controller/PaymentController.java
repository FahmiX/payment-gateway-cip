package com.cip.payment_gateway.controller;

// Service
import com.cip.payment_gateway.service.PaymentService;
// Request DTO
import com.cip.payment_gateway.dto.request.PaymentRequest;
// Response DTO
import com.cip.payment_gateway.dto.response.PaymentResponse;
// Other imports
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    // Service injection
    private final PaymentService paymentService;

    // POST /api/payments
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }

    // GET /api/payments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(response);
    }

    // DELETE API to remove payment (soft delete)
    @DeleteMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> removePayment(@PathVariable String orderId) {
        PaymentResponse response = paymentService.removePayment(orderId);
        return ResponseEntity.ok(response);
    }
}
