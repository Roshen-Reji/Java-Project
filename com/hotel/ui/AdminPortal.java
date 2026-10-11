package com.hotel.ui;

import com.hotel.domain.Admin;
import com.hotel.domain.Customer;
import com.hotel.domain.HotelStaff;
import com.hotel.domain.Payment;
import com.hotel.domain.Reservation;
import com.hotel.domain.Room;
import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomManagementService;
import com.hotel.service.StaffService;
import java.util.List;
import java.util.Scanner;

public class AdminPortal {

    public static void start(Scanner sc) {
        System.out.print("Enter Admin PIN (default: 1234): ");
        if (!sc.hasNextLine()) {
            return;
        }
        String pin = sc.nextLine().trim();
        if (!pin.equals("1234") && !pin.equalsIgnoreCase("admin")) {
            System.out.println("Invalid PIN. Access denied.");
            return;
        }

        HotelStaff existingAdmin = StaffService.findStaff(999);
        Admin admin = (existingAdmin instanceof Admin) ? (Admin) existingAdmin : new Admin(999, "Master Admin", "admin@hotel.com", "999-9999");
        System.out.println("\nWelcome, Administrator!");
        admin.displayDetails();

        boolean back = false;
        while (!back) {
            System.out.println("\n--- Administrator Portal ---");
            System.out.println("1.  View all rooms");
            System.out.println("2.  Add a new room");
            System.out.println("3.  Update an existing room");
            System.out.println("4.  Delete a room");
            System.out.println("5.  View all staff");
            System.out.println("6.  Add a new staff member");
            System.out.println("7.  Delete a staff member");
            System.out.println("8.  View all customers");
            System.out.println("9.  Delete a customer");
            System.out.println("10. View all reservations");
            System.out.println("11. View pending reservations");
            System.out.println("12. Confirm pending payment & reservation");
            System.out.println("13. Cancel a reservation");
            System.out.println("14. View all payments");
            System.out.println("15. Back to Main Menu");
            int choice = readIntChoice(sc);
            switch (choice) {
                case 1:
                    handleViewRooms();
                    break;
                case 2:
                    handleAddRoom(sc, admin);
                    break;
                case 3:
                    handleUpdateRoom(sc);
                    break;
                case 4:
                    handleDeleteRoom(sc, admin);
                    break;
                case 5:
                    handleViewStaff();
                    break;
                case 6:
                    handleAddStaff(sc, admin);
                    break;
                case 7:
                    handleDeleteStaff(sc, admin);
                    break;
                case 8:
                    handleViewCustomers();
                    break;
                case 9:
                    handleDeleteCustomer(sc);
                    break;
                case 10:
                    handleViewReservations();
                    break;
                case 11:
                    handleViewPendingReservations();
                    break;
                case 12:
                    handleConfirmReservation(sc);
                    break;
                case 13:
                    handleCancelReservation(sc);
                    break;
                case 14:
                    handleViewPayments();
                    break;
                case 15:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1-15.");
                    break;
            }
        }
    }

    private static void handleViewRooms() {
        List<Room> rooms = RoomManagementService.listAllRooms();
        if (rooms.isEmpty()) {
            System.out.println("No rooms available in system.");
            return;
        }
        System.out.println("\n--- All Rooms ---");
        for (Room r : rooms) {
            System.out.println("Room " + r.getRoomNo() + " | Type: " + r.getRoomType()
                + " | Price: $" + String.format("%.2f", r.getPrice())
                + " | Status: " + r.getStatus() + " | " + r.getDescription());
        }
    }

    private static void handleAddRoom(Scanner sc, Admin admin) {
        int roomNo = readIntPrompt(sc, "Enter new Room number: ");
        System.out.println("Choose Room Type: 1. Standard  2. Deluxe  3. Suite");
        int typeChoice = readIntChoice(sc);
        String roomType = "Standard";
        if (typeChoice == 2) {
            roomType = "Deluxe";
        } else if (typeChoice == 3) {
            roomType = "Suite";
        }
        double price = readDoublePrompt(sc, "Enter Nightly Price: ");
        System.out.print("Enter Description: ");
        String desc = sc.nextLine().trim();
        boolean created = admin.createRoom(roomNo, roomType, price, "Available", desc);
        if (created) {
            System.out.println("Room " + roomNo + " (" + roomType + ") added successfully!");
        }
    }

    private static void handleUpdateRoom(Scanner sc) {
        int roomNo = readIntPrompt(sc, "Enter Room number to update: ");
        Room existing = RoomManagementService.findRoom(roomNo);
        if (existing == null) {
            System.out.println("Error: Room " + roomNo + " does not exist.");
            return;
        }
        System.out.print("New Type (" + existing.getRoomType() + "): ");
        String type = sc.nextLine().trim();
        if (type.isEmpty()) {
            type = existing.getRoomType();
        }
        double price = readDoubleOrKeep(sc, "New Price (" + existing.getPrice() + "): ", existing.getPrice());
        System.out.print("New Status (Available/Booked/Maintenance/Pending) (" + existing.getStatus() + "): ");
        String status = sc.nextLine().trim();
        if (status.isEmpty()) {
            status = existing.getStatus();
        }
        System.out.print("New Description (" + existing.getDescription() + "): ");
        String desc = sc.nextLine().trim();
        if (desc.isEmpty()) {
            desc = existing.getDescription();
        }
        RoomManagementService.updateRoomDetails(roomNo, type, price, status, desc);
        System.out.println("Room " + roomNo + " updated successfully.");
    }

    private static void handleDeleteRoom(Scanner sc, Admin admin) {
        int roomNo = readIntPrompt(sc, "Enter Room number to delete: ");
        admin.deleteRoom(roomNo);
    }

    private static void handleViewStaff() {
        List<HotelStaff> staffList = StaffService.listAllStaff();
        if (staffList.isEmpty()) {
            System.out.println("No staff members found.");
            return;
        }
        System.out.println("\n--- All Staff Members ---");
        for (HotelStaff s : staffList) {
            s.displayDetails();
        }
    }

    private static void handleAddStaff(Scanner sc, Admin admin) {
        int staffId = readIntPrompt(sc, "Enter Staff ID: ");
        System.out.print("Enter Staff Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Enter Role (e.g. Manager, Receptionist, Housekeeping): ");
        String role = sc.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Enter Phone: ");
        String phone = sc.nextLine().trim();
        boolean created = admin.createStaff(staffId, name, role, email, phone);
        if (created) {
            System.out.println("Staff " + name + " (" + role + ") registered successfully!");
        }
    }

    private static void handleDeleteStaff(Scanner sc, Admin admin) {
        int staffId = readIntPrompt(sc, "Enter Staff ID to delete: ");
        admin.removeStaff(staffId);
    }

    private static void handleViewCustomers() {
        List<Customer> customers = AuthenticationCustomerService.listAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }
        System.out.println("\n--- Registered Customers ---");
        for (Customer c : customers) {
            c.displayDetails();
        }
    }

    private static void handleDeleteCustomer(Scanner sc) {
        int custId = readIntPrompt(sc, "Enter Customer ID to remove: ");
        AuthenticationCustomerService.deleteCustomer(custId);
    }

    private static void handleViewReservations() {
        List<Reservation> reservations = ReservationService.getAllReservations();
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        System.out.println("\n--- All Reservations ---");
        for (Reservation r : reservations) {
            String cust = (r.getCustomer() != null) ? r.getCustomer().getName() : "N/A";
            String roomInfo = (r.getRoom() != null) ? "Room " + r.getRoom().getRoomNo() : "N/A";
            System.out.println("Reservation #" + r.getReservationId()
                + " | Customer: " + cust
                + " | " + roomInfo
                + " | Dates: " + r.getCheckInDate() + " to " + r.getCheckOutDate()
                + " | Total: $" + String.format("%.2f", r.getTotalAmount())
                + " | Status: " + r.getStatus());
        }
    }

    private static void handleViewPendingReservations() {
        List<Reservation> pending = ReservationService.getPendingReservations();
        if (pending.isEmpty()) {
            System.out.println("No pending reservations found.");
            return;
        }
        System.out.println("\n--- Pending Reservations Awaiting Approval ---");
        for (Reservation r : pending) {
            String cust = (r.getCustomer() != null) ? r.getCustomer().getName() : "N/A";
            String roomInfo = (r.getRoom() != null) ? "Room " + r.getRoom().getRoomNo() : "N/A";
            String payMethod = (r.getPayment() != null) ? r.getPayment().getPaymentMethod() : "N/A";
            System.out.println("Reservation #" + r.getReservationId()
                + " | Customer: " + cust
                + " | " + roomInfo
                + " | Dates: " + r.getCheckInDate() + " to " + r.getCheckOutDate()
                + " | Total: $" + String.format("%.2f", r.getTotalAmount())
                + " | Payment Method: " + payMethod
                + " | Status: " + r.getStatus());
        }
    }

    private static void handleConfirmReservation(Scanner sc) {
        handleViewPendingReservations();
        List<Reservation> pending = ReservationService.getPendingReservations();
        if (pending.isEmpty()) {
            return;
        }
        int resId = readIntPrompt(sc, "\nEnter Reservation ID to review (0 to back): ");
        if (resId == 0) {
            return;
        }
        Reservation r = ReservationService.getReservationById(resId);
        if (r == null) {
            System.out.println("Error: Reservation #" + resId + " not found.");
            return;
        }
        if (!"Pending Approval".equalsIgnoreCase(r.getStatus()) && !"Pending".equalsIgnoreCase(r.getStatus())) {
            System.out.println("Error: Reservation #" + resId + " is not pending approval.");
            return;
        }
        System.out.println("1. Confirm Payment & Reserve Room");
        System.out.println("2. Reject Payment & Cancel Reservation");
        System.out.println("0. Back");
        int action = readIntChoice(sc);
        if (action == 1) {
            ReservationService.confirmReservation(resId);
        } else if (action == 2) {
            ReservationService.rejectReservation(resId);
        }
    }

    private static void handleCancelReservation(Scanner sc) {
        int resId = readIntPrompt(sc, "Enter Reservation ID to cancel: ");
        ReservationService.cancelReservation(resId);
    }

    private static void handleViewPayments() {
        List<Payment> payments = PaymentService.getAllPayments();
        if (payments.isEmpty()) {
            System.out.println("No payments recorded.");
            return;
        }
        System.out.println("\n--- Payment Records ---");
        for (Payment p : payments) {
            int resId = (p.getReservation() != null) ? p.getReservation().getReservationId() : 0;
            System.out.println("Payment ID #" + p.getPaymentId()
                + " | Reservation #" + resId
                + " | Method: " + p.getPaymentMethod()
                + " | Amount: $" + String.format("%.2f", p.getAmount())
                + " | Status: " + p.getPaymentStatus()
                + " | Date: " + (p.getPaymentDate() != null ? p.getPaymentDate() : "N/A"));
        }
    }

    private static int readIntChoice(Scanner sc) {
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) {
                return 15;
            }
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static int readIntPrompt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!sc.hasNextLine()) {
                return 0;
            }
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private static double readDoublePrompt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!sc.hasNextLine()) {
                return 0.0;
            }
            String line = sc.nextLine().trim();
            try {
                double val = Double.parseDouble(line);
                if (val <= 0) {
                    System.out.println("Amount must be greater than zero. Please try again.");
                    continue;
                }
                return val;
            } catch (Exception e) {
                System.out.println("Invalid amount. Please try again.");
            }
        }
    }

    private static double readDoubleOrKeep(Scanner sc, String prompt, double current) {
        System.out.print(prompt);
        if (!sc.hasNextLine()) {
            return current;
        }
        String line = sc.nextLine().trim();
        if (line.isEmpty()) {
            return current;
        }
        try {
            double val = Double.parseDouble(line);
            if (val <= 0) {
                System.out.println("Amount must be greater than zero. Keeping current value.");
                return current;
            }
            return val;
        } catch (Exception e) {
            System.out.println("Invalid amount. Keeping current value.");
            return current;
        }
    }
}
