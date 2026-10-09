package com.hotel.service;

public class PaymentGateway {

    public static boolean charge(double amount, String method) {
        if (amount <= 0) {
            System.out.println("Payment failed: invalid amount $" + String.format("%.2f", amount));
            return false;
        }
        if (method == null || method.trim().isEmpty()) {
            method = "Cash";
        }
        String txnId = "TXN" + System.currentTimeMillis();
        System.out.println(txnId + " processed payment of $" + String.format("%.2f", amount) + " via " + method + ".");
        return true;
    }

    public static boolean refund(double amount) {
        String refundId = "REF" + System.currentTimeMillis();
        System.out.println(refundId + " refunded $" + String.format("%.2f", amount) + ".");
        return true;
    }
}
