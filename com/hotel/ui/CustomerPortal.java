package com.hotel.ui;

import com.hotel.domain.Customer;
import com.hotel.domain.Payment;
import com.hotel.domain.Reservation;
import com.hotel.domain.Room;
import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.PaymentService;
import com.hotel.service.RoomManagementService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;

public class CustomerPortal {

    public static void start(Scanner sc) {
        Customer current = null;
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Customer Portal ---");
            if (current != null) {
                System.out.println("Logged in as: " + current.getName()
                    + " | " + current.getEmail() + " | " + current.getPhone()
                    + " (ID " + current.getCustomerId() + ")");
            } else {
                System.out.println("Not logged in.");
            }
            if (current == null) {
                System.out.println("1. Register  2. Login  3. View available rooms  4. Book a room  5. Cancel a reservation  6. View my bookings + bill  7. Back");
            } else {
                System.out.println("1. Register (switch account)  2. Logout (" + current.getName() + ")  3. View available rooms  4. Book a room  5. Cancel a reservation  6. View my bookings + bill  7. Back");
            }
            int choice = readIntChoice(sc);
            switch (choice) {
                case 1:
                    if (current != null) {
                        System.out.println("Note: You are logged in as " + current.getName()
                            + ". Registering will switch to a new account.");
                    }
                    Customer created = handleRegister(sc);
                    if (created != null) {
                        current = created;
                    }
                    break;
                case 2:
                    if (current == null) {
                        Customer logged = handleLogin(sc);
                        if (logged != null) {
                            current = logged;
                        }
                    } else {
                        System.out.println("Goodbye, " + current.getName() + ". Logged out.");
                        current = null;
                    }
                    break;
                case 3:
                    handleViewRooms();
                    break;
                case 4:
                    if (!requireLogin(current)) {
                        break;
                    }
                    handleBookRoom(sc, current);
                    break;
                case 5:
                    if (!requireLogin(current)) {
                        break;
                    }
                    handleCancel(sc, current);
                    break;
                case 6:
                    if (!requireLogin(current)) {
                        break;
                    }
                    handleViewBookings(current);
                    break;
                case 7:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1-7.");
                    break;
            }
        }
    }

    private static boolean requireLogin(Customer current) {
        if (current == null) {
            System.out.println("Please register or login first.");
            return false;
        }
        return true;
    }

    private static Customer handleRegister(Scanner sc) {
        String name = "";
        while (true) {
            System.out.print("Name: ");
            if (!sc.hasNextLine()) return null;
            name = sc.nextLine().trim();
            if (!name.isEmpty()) {
                break;
            }
            System.out.println("Error: Name cannot be empty.");
        }
        String phone = "";
        while (true) {
            System.out.print("Phone (7-15 digits, e.g. +1 555-123-4567): ");
            if (!sc.hasNextLine()) return null;
            phone = sc.nextLine().trim();
            if (AuthenticationCustomerService.isValidPhone(phone)) {
                break;
            }
            System.out.println("Error: Invalid phone. Use 7-15 digits.");
        }
        String email = "";
        while (true) {
            System.out.print("Email (e.g. name@example.com): ");
            if (!sc.hasNextLine()) return null;
            email = sc.nextLine().trim();
            if (AuthenticationCustomerService.isValidEmail(email)) {
                break;
            }
            System.out.println("Error: Invalid email format. Use like name@example.com.");
        }
        System.out.print("Address: ");
        if (!sc.hasNextLine()) return null;
        String address = sc.nextLine().trim();
        Customer c = AuthenticationCustomerService.registerCustomer(name, phone, email, address);
        if (c != null) {
            System.out.println("Registered successfully! Your customer ID is " + c.getCustomerId() + ".");
            return c;
        }
        return null;
    }

    private static Customer handleLogin(Scanner sc) {
        System.out.print("Email or phone: ");
        if (!sc.hasNextLine()) return null;
        String key = sc.nextLine().trim();
        if (key.isEmpty()) {
            System.out.println("Error: Please enter your email or phone.");
            return null;
        }
        Customer c = AuthenticationCustomerService.login(key);
        if (c == null) {
            System.out.println("Error: No customer found with \"" + key + "\". Check email/phone or register first.");
            return null;
        }
        System.out.println("Welcome back, " + c.getName() + "!");
        return c;
    }

    private static void handleViewRooms() {
        List<Room> rooms = RoomManagementService.listAvailableRooms();
        if (rooms.isEmpty()) {
            System.out.println("No available rooms.");
            return;
        }
        System.out.println("Available rooms:");
        for (Room r : rooms) {
            System.out.println("Room " + r.getRoomNo() + " | " + r.getRoomType() + " | $" + String.format("%.2f", r.getPrice()) + "/night | " + r.getDescription());
        }
    }

    private static void handleBookRoom(Scanner sc, Customer current) {
        List<Room> available = RoomManagementService.listAvailableRooms();
        if (available.isEmpty()) {
            System.out.println("No available rooms for booking.");
            return;
        }
        handleViewRooms();
        int roomNo = readIntPrompt(sc, "Enter Room no to book: ");
        Room targetRoom = RoomManagementService.findRoom(roomNo);
        if (targetRoom == null || !targetRoom.checkAvailability()) {
            System.out.println("Error: Room " + roomNo + " is not available for booking.");
            return;
        }
        LocalDate checkIn = readDatePrompt(sc, "Check-in Date (YYYY-MM-DD): ");
        LocalDate checkOut = readDatePrompt(sc, "Check-out Date (YYYY-MM-DD): ");
        if (!checkOut.isAfter(checkIn)) {
            System.out.println("Error: Check-out date must be after check-in date.");
            return;
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double total = nights * targetRoom.getPrice();
        System.out.println("Total Amount: $" + String.format("%.2f", total) + " (" + nights + " nights @ $" + String.format("%.2f", targetRoom.getPrice()) + "/night)");

        System.out.println("\nSelect Payment Method:");
        System.out.println("1. UPI");
        System.out.println("2. Card");
        System.out.println("3. Cash");
        int payChoice = 0;
        while (payChoice < 1 || payChoice > 3) {
            payChoice = readIntChoice(sc);
            if (payChoice < 1 || payChoice > 3) {
                System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
        String paymentMethod = "Cash";
        String paymentDetail = "";

        if (payChoice == 1) {
            paymentMethod = "UPI";
            System.out.print("Enter UPI ID (e.g. name@upi): ");
            paymentDetail = sc.nextLine().trim();
            if (paymentDetail.isEmpty()) {
                paymentDetail = "user@upi";
            }
        } else if (payChoice == 2) {
            paymentMethod = "Card";
            System.out.print("Enter Card Number: ");
            String cardNum = sc.nextLine().trim();
            if (cardNum.isEmpty()) {
                cardNum = "1234567890123456";
            }
            System.out.print("Enter Card Holder Name: ");
            String cardHolder = sc.nextLine().trim();
            if (cardHolder.isEmpty()) {
                cardHolder = current.getName();
            }
            paymentDetail = cardNum + ";" + cardHolder;
        } else {
            paymentMethod = "Cash";
            System.out.println("Cash payment selected. Please pay at hotel front desk.");
            paymentDetail = "Cash at counter";
        }

        Reservation res = current.bookRoom(roomNo, checkIn, checkOut, paymentMethod, paymentDetail);
        if (res != null) {
            System.out.println("\nBooking Request Submitted!");
            System.out.println("Reservation ID : #" + res.getReservationId());
            System.out.println("Status         : " + res.getStatus());
            System.out.println("Payment Method : " + paymentMethod);
            System.out.println("Payment Status : Pending Confirmation");
            System.out.println("Note: The room will be officially reserved after Staff/Admin confirms your payment.");
        }
    }

    private static void handleCancel(Scanner sc, Customer current) {
        int resId = readIntPrompt(sc, "Enter Reservation ID to cancel: ");
        current.cancelReservation(resId);
    }

    private static void handleViewBookings(Customer current) {
        List<Reservation> bookings = current.viewBooking();
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        for (Reservation r : bookings) {
            long nights = 0;
            try {
                nights = ChronoUnit.DAYS.between(r.getCheckInDate(), r.getCheckOutDate());
            } catch (Exception e) {
                nights = 0;
            }
            String roomInfo = "N/A";
            if (r.getRoom() != null) {
                roomInfo = "Room " + r.getRoom().getRoomNo() + " (" + r.getRoom().getRoomType() + ")";
            }
            System.out.println("\n-------------------------------------------");
            System.out.println("Reservation #" + r.getReservationId() + " | " + roomInfo);
            System.out.println("Dates          : " + r.getCheckInDate() + " to " + r.getCheckOutDate() + " (" + nights + " nights)");
            System.out.println("Total Amount   : $" + String.format("%.2f", r.getTotalAmount()));
            System.out.println("Booking Status : " + r.getStatus());
            Payment pay = r.getPayment() != null ? r.getPayment() : PaymentService.getPaymentRepository().findByReservationId(r.getReservationId());
            if (pay != null) {
                PaymentService.generateBill(r);
            } else {
                System.out.println("(No payment record)");
            }
            System.out.println("-------------------------------------------");
        }
    }

    private static int readIntChoice(Scanner sc) {
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) {
                return 7;
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

    private static LocalDate readDatePrompt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!sc.hasNextLine()) {
                return LocalDate.now().plusDays(1);
            }
            String line = sc.nextLine().trim();
            try {
                return LocalDate.parse(line);
            } catch (Exception e) {
                System.out.println("Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }
}
