package com.cip.payment_gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.cip.payment_gateway.dto.request.CoreBankRequest;
import com.cip.payment_gateway.dto.response.CoreBankResponse;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/corebank")
public class CoreBankFeignController {

    @PostMapping("/debit")
    public CoreBankResponse debit(@RequestBody CoreBankRequest request) {
        // Simulate debit operation
        // Balance
        BigDecimal balance = new BigDecimal("500000");

        // If amount is greater than balance,
        // return failed response cause insufficient funds
        if (request.getAmount().compareTo(balance) > 0) {
            return CoreBankResponse.builder()
                    .coreBankReference("CB-" + request.getAccount() + "-" + System.currentTimeMillis())
                    .status("FAILED")
                    .build();
        }
        // Otherwise, return success response
        // cause debit is successful (balance is sufficient)
        return CoreBankResponse.builder()
                .coreBankReference("CB-" + request.getAccount() + "-" + System.currentTimeMillis())
                .status("SUCCESS")
                .build();
    }
}
