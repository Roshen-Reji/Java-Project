package com.hotel.service;

import com.hotel.domain.CardPayment;
import com.hotel.domain.CashPayment;
import com.hotel.domain.Payment;
import com.hotel.domain.Reservation;
import com.hotel.domain.UpiPayment;
import com.hotel.repository.PaymentRepository;
import java.util.List;

public class PaymentService {
    private static final PaymentRepository paymentRepository = new PaymentRepository();

    public static PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }

    public static Payment createPendingPayment(Reservation reservation, String method, String detail) {
        if (reservation == null) {
            System.out.println("Error: Cannot process payment for null reservation.");
            return null;
        }
        Payment payment;
        if ("UPI".equalsIgnoreCase(method)) {
            payment = new UpiPayment(0, reservation.getTotalAmount(), detail != null ? detail : "N/A");
        } else if ("Card".equalsIgnoreCase(method)) {
            String cardNum = detail;
            String cardHolder = "Card Holder";
            if (detail != null && detail.contains(";")) {
                String[] parts = detail.split(";", 2);
                cardNum = parts[0];
                cardHolder = parts[1];
            }
            payment = new CardPayment(0, reservation.getTotalAmount(), cardNum, cardHolder);
        } else {
            payment = new CashPayment(0, reservation.getTotalAmount());
        }
        payment.setReservation(reservation);
        reservation.setPayment(payment);
        paymentRepository.add(payment);
        return payment;
    }

    public static boolean confirmPaymentForReservation(int reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId);
        if (payment == null) {
            System.out.println("Error: No payment record found for Reservation #" + reservationId);
            return false;
        }
        boolean charged = PaymentGateway.charge(payment.getAmount(), payment.getPaymentMethod());
        if (charged) {
            payment.confirmPayment();
            paymentRepository.update(payment);
            System.out.println("Payment for Reservation #" + reservationId + " confirmed successfully.");
            return true;
        } else {
            payment.setPaymentStatus("Failed");
            paymentRepository.update(payment);
            System.out.println("Payment confirmation failed.");
            return false;
        }
    }

    public static void rejectPaymentForReservation(int reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId);
        if (payment != null) {
            payment.setPaymentStatus("Rejected");
            paymentRepository.update(payment);
        }
    }

    public static void refundPayment(Payment payment) {
        if (payment == null) {
            return;
        }
        boolean ok = PaymentGateway.refund(payment.getAmount());
        if (ok) {
            payment.setPaymentStatus("Refunded");
            paymentRepository.update(payment);
        }
    }

    public static void generateBill(Reservation reservation) {
        if (reservation == null) {
            System.out.println("Error: Reservation not found.");
            return;
        }
        Payment payment = reservation.getPayment();
        if (payment == null) {
            payment = paymentRepository.findByReservationId(reservation.getReservationId());
        }
        if (payment == null) {
            System.out.println("No payment found for Reservation #" + reservation.getReservationId());
            return;
        }
        payment.generateBill();
    }

    public static boolean updatePaymentStatus(int paymentId, String newStatus) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            System.out.println("Error: Payment status cannot be empty.");
            return false;
        }
        boolean ok = paymentRepository.updatePaymentStatus(paymentId, newStatus.trim());
        if (ok) {
            System.out.println("Payment #" + paymentId + " status updated to " + newStatus.trim());
        } else {
            System.out.println("Error: Could not update status for Payment #" + paymentId);
        }
        return ok;
    }

    public static List<Payment> getAllPayments() {
        return paymentRepository.getAll();
    }
}
