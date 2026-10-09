package com.hotel.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Payment {
    protected int paymentId;
    protected double amount;
    protected String paymentMethod;
    protected String paymentStatus;
    protected LocalDate paymentDate;
    protected Reservation reservation;

    public Payment(int paymentId, double amount, String paymentMethod, String paymentStatus) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paymentDate = null;
        this.reservation = null;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void makePayment() {
        this.paymentStatus = "Pending Confirmation";
    }

    public void confirmPayment() {
        this.paymentStatus = "Paid";
        this.paymentDate = LocalDate.now();
    }

    public void generateBill() {
        if (reservation == null) {
            System.out.println("Error: No reservation linked to this payment.");
            return;
        }
        long nights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        double rate = 0.0;
        String roomInfo = "N/A";
        String customerName = "N/A";
        if (reservation.getRoom() != null) {
            rate = reservation.getRoom().getPrice();
            roomInfo = reservation.getRoom().getRoomNo() + " " + reservation.getRoom().getRoomType();
        }
        if (reservation.getCustomer() != null) {
            customerName = reservation.getCustomer().getName();
        }
        System.out.println("--- Bill ---");
        System.out.println("Reservation ID : " + reservation.getReservationId());
        System.out.println("Customer       : " + customerName);
        System.out.println("Room           : " + roomInfo);
        System.out.println("Check-in       : " + reservation.getCheckInDate());
        System.out.println("Check-out      : " + reservation.getCheckOutDate());
        System.out.println("Nights         : " + nights);
        System.out.println("Nightly rate   : $" + String.format("%.2f", rate));
        System.out.println("Total amount   : $" + String.format("%.2f", reservation.getTotalAmount()));
        System.out.println("Payment method : " + paymentMethod);
        System.out.println("Payment status : " + paymentStatus);
        if (paymentDate != null) {
            System.out.println("Payment date   : " + paymentDate);
        }
    }
}
