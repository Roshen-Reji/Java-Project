package com.hotel.domain;

public class UpiPayment extends Payment {
    private String upiId;

    public UpiPayment(int paymentId, double amount, String upiId) {
        super(paymentId, amount, "UPI", "Pending Confirmation");
        this.upiId = upiId;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public void generateBill() {
        super.generateBill();
        System.out.println("UPI ID         : " + upiId);
    }
}
