package com.cip.payment_gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.cip.payment_gateway.dto.request.CoreBankRequest;
import com.cip.payment_gateway.dto.response.CoreBankResponse;

@FeignClient(name = "core-bank-client", url = "${corebank.url}")

public interface CoreBankFeignClient {

    @PostMapping("/api/corebank/debit")
    CoreBankResponse debit(
            @RequestBody CoreBankRequest request);

}
