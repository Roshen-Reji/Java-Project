package com.hotel.domain;

public class CashPayment extends Payment {
    public CashPayment(int paymentId, double amount) {
        super(paymentId, amount, "Cash", "Pending Confirmation");
    }

    @Override
    public void generateBill() {
        super.generateBill();
        System.out.println("Payment Details: Counter Cash Collection");
    }
}
