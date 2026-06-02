package com.cip.payment_gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateOrderException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateOrderException(DuplicateOrderException ex) {

        ErrorResponse response = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMeessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .message("Invalid request body: " + ex.getMostSpecificCause().getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
