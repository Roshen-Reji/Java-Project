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
        if (checkIn.isBefore(LocalDate.now())) {
            String err = "Check-in date cannot be in the past.";
            System.out.println("Error: " + err);
            throw new IllegalArgumentException(err);
        }
        if (!checkOut.isAfter(checkIn)) {
            String err = "Check-out date must be after check-in date.";
            System.out.println("Error: " + err);
            throw new IllegalArgumentException(err);
        }
        if ("Maintenance".equalsIgnoreCase(room.getStatus())) {
            String err = "Room " + room.getRoomNo() + " is currently under maintenance.";
            System.out.println("Error: " + err);
            throw new IllegalArgumentException(err);
        }

        if (!reservationRepository.isRoomAvailableForDateRange(room.getRoomNo(), checkIn, checkOut)) {
            List<Reservation> active = reservationRepository.findActiveReservationsByRoomId(room.getRoomNo());
            for (Reservation ex : active) {
                if (ex.getCheckInDate() != null && ex.getCheckOutDate() != null) {
                    if (checkIn.isBefore(ex.getCheckOutDate()) && checkOut.isAfter(ex.getCheckInDate())) {
                        String errMsg = "Room #" + room.getRoomNo() + " is already reserved from " +
                                ex.getCheckInDate() + " to " + ex.getCheckOutDate() +
                                ". You can book it before " + ex.getCheckInDate() +
                                " (Check-Out on or before " + ex.getCheckInDate() + ") or from " +
                                ex.getCheckOutDate() + " onwards.";
                        System.out.println("Error: " + errMsg);
                        throw new IllegalArgumentException(errMsg);
                    }
                }
            }
            String errMsg = "Room #" + room.getRoomNo() + " is not available for the selected dates.";
            System.out.println("Error: " + errMsg);
            throw new IllegalArgumentException(errMsg);
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalAmount = nights * room.getPrice();
        Reservation r = new Reservation(0, customer, room, checkIn, checkOut, "Pending Approval", totalAmount);
        reservationRepository.add(r);

        // Only update room status to Pending if the reservation starts today
        if (!checkIn.isAfter(LocalDate.now())) {
            RoomManagementService.updateStatus(room.getRoomNo(), "Pending");
        }
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
                LocalDate today = LocalDate.now();
                if (r.getCheckInDate() != null && !r.getCheckInDate().isAfter(today) && r.getCheckOutDate().isAfter(today)) {
                    RoomManagementService.updateStatus(r.getRoom().getRoomNo(), "Booked");
                } else {
                    RoomManagementService.updateStatus(r.getRoom().getRoomNo(), "Available");
                }
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

    public static Reservation getActiveReservationForRoom(int roomId) {
        return reservationRepository.findActiveReservationByRoomId(roomId);
    }
}
