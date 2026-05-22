package com.cip.payment_gateway.exception;

public class DuplicateOrderException
        extends RuntimeException {

    public DuplicateOrderException(
            String message) {
        super(message);
    }
}
