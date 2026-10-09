package com.hotel.domain;

public class CardPayment extends Payment {
    private String cardNumber;
    private String cardHolderName;

    public CardPayment(int paymentId, double amount, String cardNumber, String cardHolderName) {
        super(paymentId, amount, "Card", "Pending Confirmation");
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    @Override
    public void generateBill() {
        super.generateBill();
        String masked = cardNumber != null && cardNumber.length() > 4 ? "**** **** **** " + cardNumber.substring(cardNumber.length() - 4) : cardNumber;
        System.out.println("Card Holder    : " + cardHolderName);
        System.out.println("Card Number    : " + masked);
    }
}
