package com.hotel.domain;

import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomManagementService;
import java.time.LocalDate;
import java.util.List;

public class Customer extends Person {
    private String address;

    public Customer(int customerId, String name, String phone, String email, String address) {
        super(customerId, name, phone, email);
        this.address = address;
    }

    public int getCustomerId() {
        return getId();
    }

    public void setCustomerId(int customerId) {
        setId(customerId);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Reservation bookRoom(int roomNo, LocalDate checkIn, LocalDate checkOut, String paymentMethod, String paymentDetail) {
        Room room = RoomManagementService.findRoom(roomNo);
        if (room == null || !room.checkAvailability()) {
            System.out.println("Error: Room " + roomNo + " not found or not available.");
            return null;
        }
        Reservation reservation = ReservationService.createReservation(this, room, checkIn, checkOut);
        if (reservation == null) {
            return null;
        }
        PaymentService.createPendingPayment(reservation, paymentMethod, paymentDetail);
        return reservation;
    }

    public void cancelReservation(int reservationId) {
        Reservation r = ReservationService.getReservationById(reservationId);
        if (r == null || r.getCustomer() == null || r.getCustomer().getCustomerId() != this.getCustomerId()) {
            System.out.println("Error: Reservation #" + reservationId + " not found in your bookings.");
            return;
        }
        if ("Cancelled".equalsIgnoreCase(r.getStatus())) {
            System.out.println("Error: Reservation #" + reservationId + " is already cancelled.");
            return;
        }
        ReservationService.cancelReservation(reservationId);
    }

    public List<Reservation> viewBooking() {
        return ReservationService.getReservationsForCustomer(this.getCustomerId());
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Role: Customer | Address: " + address);
    }
}
