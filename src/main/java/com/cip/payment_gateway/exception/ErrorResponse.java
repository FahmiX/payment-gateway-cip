package com.cip.payment_gateway.exception;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {

    private String message;

    private int status;

    private LocalDateTime timestamp;
}
