package com.cip.payment_gateway.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoreBankResponse {
    // Total fields: 2
    // CoreBankReference, status

    private String coreBankReference;
    private String status;
}
