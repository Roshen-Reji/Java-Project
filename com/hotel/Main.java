package com.hotel;

import com.hotel.database.DatabaseInitializer;
import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomManagementService;
import com.hotel.service.StaffService;
import com.hotel.ui.AdminPortal;
import com.hotel.ui.CustomerPortal;
import com.hotel.ui.StaffPortal;
import com.hotel.ui.swing.HotelAppFrame;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Step 1: Initialize Database and Schema / Seed Data
        DatabaseInitializer.initialize();

        // Step 2: Check for --cli argument for legacy console mode
        boolean cliMode = false;
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if ("--cli".equalsIgnoreCase(arg) || "-cli".equalsIgnoreCase(arg) || "cli".equalsIgnoreCase(arg)) {
                    cliMode = true;
                    break;
                }
            }
        }

        if (cliMode) {
            runCliMenu();
        } else {
            // Step 3: Default — Launch modern Java Swing GUI on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                AuthenticationCustomerService authService = new AuthenticationCustomerService();
                RoomManagementService roomService = new RoomManagementService();
                ReservationService resService = new ReservationService();
                PaymentService payService = new PaymentService();
                StaffService staffService = new StaffService();

                HotelAppFrame appFrame = new HotelAppFrame(authService, roomService, resService, payService, staffService);
                appFrame.setVisible(true);
            });
        }
    }

    private static void runCliMenu() {
        Scanner sc = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Hotel Reservation System (Console Mode) ===");
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
