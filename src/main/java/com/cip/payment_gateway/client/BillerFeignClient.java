package com.cip.payment_gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.cip.payment_gateway.dto.request.BillerRequest;
import com.cip.payment_gateway.dto.response.BillerResponse;

@FeignClient(name = "biller-client", url = "${biller.url}")

public interface BillerFeignClient {

    @PostMapping("/api/biller/pay")
    BillerResponse pay(@RequestBody BillerRequest request);

}
