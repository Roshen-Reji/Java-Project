package com.hotel.ui;

import com.hotel.domain.Customer;
import com.hotel.domain.Reservation;
import com.hotel.domain.Room;
import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomManagementService;
import java.util.List;
import java.util.Scanner;

public class StaffPortal {

    public static void start(Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Hotel Staff Portal ---");
            System.out.println("1. View all rooms");
            System.out.println("2. Update room status / details");
            System.out.println("3. View pending reservations");
            System.out.println("4. Confirm pending payment & reservation");
            System.out.println("5. View all reservations");
            System.out.println("6. View all customers");
            System.out.println("7. Cancel a reservation");
            System.out.println("8. Back");
            int choice = readIntChoice(sc);
            switch (choice) {
                case 1:
                    handleViewAllRooms();
                    break;
                case 2:
                    handleUpdateRoom(sc);
                    break;
                case 3:
                    handleViewPendingReservations();
                    break;
                case 4:
                    handleConfirmPendingPayment(sc);
                    break;
                case 5:
                    handleViewReservations();
                    break;
                case 6:
                    handleViewCustomers();
                    break;
                case 7:
                    handleCancelReservation(sc);
                    break;
                case 8:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1-8.");
                    break;
            }
        }
    }

    private static void handleViewAllRooms() {
        List<Room> rooms = RoomManagementService.listAllRooms();
        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }
        System.out.println("\n--- All Rooms ---");
        for (Room r : rooms) {
            System.out.println("Room " + r.getRoomNo() + " | Type: " + r.getRoomType()
                + " | Price: $" + String.format("%.2f", r.getPrice())
                + " | Status: " + r.getStatus() + " | " + r.getDescription());
        }
    }

    private static void handleUpdateRoom(Scanner sc) {
        int roomNo = readIntPrompt(sc, "Room no to update: ");
        Room existing = RoomManagementService.findRoom(roomNo);
        if (existing == null) {
            System.out.println("Error: Room " + roomNo + " not found.");
            return;
        }
        System.out.print("New room type (" + existing.getRoomType() + "): ");
        String type = sc.nextLine().trim();
        if (type.isEmpty()) {
            type = existing.getRoomType();
        }
        double price = readDoubleOrKeep(sc, "New price (" + existing.getPrice() + "): ", existing.getPrice());
        System.out.print("New status (Available/Booked/Maintenance/Pending) (" + existing.getStatus() + "): ");
        String status = sc.nextLine().trim();
        if (status.isEmpty()) {
            status = existing.getStatus();
        }
        System.out.print("New description (" + existing.getDescription() + "): ");
        String desc = sc.nextLine().trim();
        if (desc.isEmpty()) {
            desc = existing.getDescription();
        }
        RoomManagementService.updateRoomDetails(roomNo, type, price, status, desc);
        System.out.println("Room " + roomNo + " updated successfully.");
    }

    private static void handleViewPendingReservations() {
        List<Reservation> pending = ReservationService.getPendingReservations();
        if (pending.isEmpty()) {
            System.out.println("No pending reservations found.");
            return;
        }
        System.out.println("\n--- Pending Reservations Awaiting Confirmation ---");
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

    private static void handleConfirmPendingPayment(Scanner sc) {
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

    private static void handleViewCustomers() {
        List<Customer> customers = AuthenticationCustomerService.listAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }
        System.out.println("\n--- Registered Customers ---");
        for (Customer c : customers) {
            System.out.println("ID: " + c.getCustomerId() + " | Name: " + c.getName()
                + " | Phone: " + c.getPhone() + " | Email: " + c.getEmail() + " | Address: " + c.getAddress());
        }
    }

    private static void handleCancelReservation(Scanner sc) {
        int resId = readIntPrompt(sc, "Enter Reservation ID to cancel: ");
        ReservationService.cancelReservation(resId);
    }

    private static int readIntChoice(Scanner sc) {
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) {
                return 8;
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
