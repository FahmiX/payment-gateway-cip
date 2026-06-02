package com.cip.payment_gateway.enums;

public enum TransactionChannel {
    MOBILE_BANKING,
    INTERNET_BANKING,
    ATM;

    public static TransactionChannel from(String channel) {
        for (TransactionChannel tc : TransactionChannel.values()) {
            if (tc.name().equalsIgnoreCase(channel)) {
                return tc;
            }
        }
        throw new IllegalArgumentException("Invalid transaction channel: " + channel);
    }
}