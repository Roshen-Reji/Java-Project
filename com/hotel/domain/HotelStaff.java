package com.hotel.domain;

import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomManagementService;
import java.util.List;

public class HotelStaff extends Person {
    protected String role;
    protected String password = "";

    public HotelStaff(int staffId, String name, String role, String email, String phone) {
        super(staffId, name, phone, email);
        this.role = role;
    }

    public HotelStaff(int staffId, String name, String role, String email, String phone, String password) {
        super(staffId, name, phone, email);
        this.role = role;
        this.password = password != null ? password : "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password != null ? password : "";
    }

    public int getStaffId() {
        return getId();
    }

    public void setStaffId(int staffId) {
        setId(staffId);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void manageRooms() {
        List<Room> rooms = RoomManagementService.listAllRooms();
        System.out.println("--- All Rooms ---");
        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }
        for (Room r : rooms) {
            System.out.println("Room " + r.getRoomNo() + " | " + r.getRoomType()
                + " | $" + String.format("%.2f", r.getPrice())
                + " | " + r.getStatus() + " | " + r.getDescription());
        }
    }

    public void manageCustomers() {
        List<Customer> customers = AuthenticationCustomerService.listAllCustomers();
        System.out.println("--- All Customers ---");
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer c : customers) {
            System.out.println("ID " + c.getCustomerId() + " | " + c.getName()
                + " | " + c.getPhone() + " | " + c.getEmail() + " | " + c.getAddress());
        }
    }

    public void viewReservations() {
        List<Reservation> reservations = ReservationService.getAllReservations();
        System.out.println("--- All Reservations ---");
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        for (Reservation r : reservations) {
            String cust = (r.getCustomer() != null) ? r.getCustomer().getName() : "N/A";
            String roomInfo = (r.getRoom() != null) ? String.valueOf(r.getRoom().getRoomNo()) : "N/A";
            System.out.println("Reservation #" + r.getReservationId()
                + " | Customer: " + cust
                + " | Room: " + roomInfo
                + " | " + r.getCheckInDate() + " to " + r.getCheckOutDate()
                + " | Total: $" + String.format("%.2f", r.getTotalAmount())
                + " | " + r.getStatus());
        }
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Role: " + role);
    }
}
