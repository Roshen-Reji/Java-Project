package com.hotel.service;

import com.hotel.domain.Customer;
import com.hotel.domain.Payment;
import com.hotel.domain.Reservation;
import com.hotel.domain.Room;
import com.hotel.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    private static final ReservationRepository reservationRepository = new ReservationRepository();

    public static ReservationRepository getReservationRepository() {
        return reservationRepository;
    }

    public static Reservation createReservation(Customer customer, Room room, LocalDate checkIn, LocalDate checkOut) {
        if (customer == null || room == null || checkIn == null || checkOut == null) {
            System.out.println("Error: Reservation details cannot be null.");
            return null;
        }
        if (!checkOut.isAfter(checkIn)) {
            System.out.println("Error: Check-out date must be after check-in date.");
            return null;
        }
        if (!room.checkAvailability()) {
            System.out.println("Error: Room " + room.getRoomNo() + " is not available.");
            return null;
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalAmount = nights * room.getPrice();
        Reservation r = new Reservation(0, customer, room, checkIn, checkOut, "Pending Approval", totalAmount);
        reservationRepository.add(r);
        RoomManagementService.updateStatus(room.getRoomNo(), "Pending");
        return r;
    }

    public static boolean confirmReservation(int reservationId) {
        Reservation r = reservationRepository.getById(reservationId);
        if (r == null) {
            System.out.println("Error: Reservation #" + reservationId + " not found.");
            return false;
        }
        if ("Confirmed".equalsIgnoreCase(r.getStatus())) {
            System.out.println("Reservation #" + reservationId + " is already confirmed.");
            return false;
        }
        if ("Cancelled".equalsIgnoreCase(r.getStatus())) {
            System.out.println("Cannot confirm a cancelled reservation.");
            return false;
        }
        boolean paymentConfirmed = PaymentService.confirmPaymentForReservation(reservationId);
        if (paymentConfirmed) {
            r.confirmBooking();
            if (r.getRoom() != null) {
                RoomManagementService.updateStatus(r.getRoom().getRoomNo(), "Booked");
            }
            reservationRepository.update(r);
            System.out.println("Reservation #" + reservationId + " is now confirmed and room is reserved!");
            return true;
        }
        return false;
    }

    public static boolean rejectReservation(int reservationId) {
        Reservation r = reservationRepository.getById(reservationId);
        if (r == null) {
            System.out.println("Error: Reservation #" + reservationId + " not found.");
            return false;
        }
        if ("Cancelled".equalsIgnoreCase(r.getStatus())) {
            System.out.println("Error: Reservation #" + reservationId + " is already cancelled.");
            return false;
        }
        if ("Confirmed".equalsIgnoreCase(r.getStatus())) {
            System.out.println("Error: Reservation #" + reservationId + " is already confirmed. Use cancel instead.");
            return false;
        }
        r.cancelBooking();
        if (r.getRoom() != null) {
            RoomManagementService.updateStatus(r.getRoom().getRoomNo(), "Available");
        }
        PaymentService.rejectPaymentForReservation(reservationId);
        reservationRepository.update(r);
        System.out.println("Reservation #" + reservationId + " has been rejected.");
        return true;
    }

    public static boolean cancelReservation(int reservationId) {
        Reservation r = reservationRepository.getById(reservationId);
        if (r == null) {
            System.out.println("Error: Reservation #" + reservationId + " not found.");
            return false;
        }
        if ("Cancelled".equalsIgnoreCase(r.getStatus())) {
            System.out.println("Error: Reservation #" + reservationId + " is already cancelled.");
            return false;
        }
        r.cancelBooking();
        if (r.getRoom() != null) {
            RoomManagementService.updateStatus(r.getRoom().getRoomNo(), "Available");
        }
        Payment payment = r.getPayment();
        if (payment == null) {
            payment = PaymentService.getPaymentRepository().findByReservationId(reservationId);
        }
        if (payment != null) {
            if ("Paid".equalsIgnoreCase(payment.getPaymentStatus())) {
                PaymentService.refundPayment(payment);
            } else {
                payment.setPaymentStatus("Cancelled");
                PaymentService.getPaymentRepository().update(payment);
            }
        }
        reservationRepository.update(r);
        System.out.println("Reservation #" + reservationId + " has been successfully cancelled.");
        return true;
    }

    public static Reservation getReservationById(int reservationId) {
        return reservationRepository.getById(reservationId);
    }

    public static List<Reservation> getAllReservations() {
        return reservationRepository.getAll();
    }

    public static List<Reservation> getReservationsForCustomer(int customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }

    public static List<Reservation> getPendingReservations() {
        List<Reservation> pending = new ArrayList<>();
        for (Reservation r : reservationRepository.getAll()) {
            if ("Pending".equalsIgnoreCase(r.getStatus()) || "Pending Approval".equalsIgnoreCase(r.getStatus())) {
                pending.add(r);
            }
        }
        return pending;
    }
}
