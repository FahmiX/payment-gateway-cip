package com.cip.payment_gateway.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillerResponse {
    // Total fields: 2
    // billerReference, status

    private String billerReference;
    private String status;
}
