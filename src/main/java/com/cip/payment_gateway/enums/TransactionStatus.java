package com.cip.payment_gateway.enums;

public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REMOVED;

    public static TransactionStatus from(String status) {
        for (TransactionStatus ts : TransactionStatus.values()) {
            if (ts.name().equalsIgnoreCase(status)) {
                return ts;
            }
        }
        throw new IllegalArgumentException("Invalid transaction status: " + status);
    }
}
