package com.hotel;

import com.hotel.domain.Admin;
import com.hotel.domain.HotelStaff;
import com.hotel.service.RoomManagementService;
import com.hotel.service.StaffService;
import com.hotel.ui.AdminPortal;
import com.hotel.ui.CustomerPortal;
import com.hotel.ui.StaffPortal;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        seedData();
        Scanner sc = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Hotel Reservation System ===");
            System.out.println("1. Customer Portal");
            System.out.println("2. Staff Portal");
            System.out.println("3. Admin Portal");
            System.out.println("4. Exit");
            int choice = readIntChoice(sc);
            switch (choice) {
                case 1:
                    CustomerPortal.start(sc);
                    break;
                case 2:
                    StaffPortal.start(sc);
                    break;
                case 3:
                    AdminPortal.start(sc);
                    break;
                case 4:
                    exit = true;
                    System.out.println("Thank you for using Hotel Reservation System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1-4.");
                    break;
            }
        }
        sc.close();
    }

    private static void seedData() {
        StaffService.addStaff(new HotelStaff(1, "Alice", "Manager", "alice@hotel.com", "111-1111"));
        StaffService.addStaff(new HotelStaff(2, "Bob", "Receptionist", "bob@hotel.com", "222-2222"));
        StaffService.addStaff(new Admin(999, "Master Admin", "admin@hotel.com", "999-9999"));

        RoomManagementService.addRoom(101, "Deluxe", 80.0, "Available", "Sea view deluxe room");
        RoomManagementService.addRoom(102, "Standard", 50.0, "Available", "Cozy standard room");
        RoomManagementService.addRoom(103, "Suite", 150.0, "Available", "Luxury suite with balcony");
        RoomManagementService.addRoom(104, "Standard", 55.0, "Available", "Standard room with garden view");
        RoomManagementService.addRoom(105, "Deluxe", 90.0, "Maintenance", "Deluxe room under maintenance");

        System.out.println("Initial system data seeded: 3 Staff/Admin members and 5 Rooms ready.");
    }

    private static int readIntChoice(Scanner sc) {
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
