package com.cip.payment_gateway.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.cip.payment_gateway.dto.request.BillerRequest;
import com.cip.payment_gateway.dto.response.BillerResponse;

@RestController
@RequestMapping("/api/biller")
public class BillerFeignController {

    @PostMapping("/pay")
    public BillerResponse pay(@RequestBody BillerRequest request) {
        // Simulate biller payment processing
        // Assume the payment is successfull
        boolean isPaymentSuccessful = true;

        if (!isPaymentSuccessful) {
            return BillerResponse.builder()
                    .billerReference("BL-" + request.getOrderId() + "-" + System.currentTimeMillis())
                    .status("FAILED")
                    .build();
        }

        return BillerResponse.builder()
                .billerReference("BL-" + request.getOrderId() + "-" + System.currentTimeMillis())
                .status("SUCCESS")
                .build();
    }
}
