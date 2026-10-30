package com.hotel.ui;

import com.hotel.database.DatabaseConnection;
import com.hotel.database.DatabaseInitializer;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DatabaseInspectorCLI {

    private static final String[] KNOWN_TABLES = {"customers", "staff", "rooms", "reservations", "payments"};

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        if (args != null && args.length > 0) {
            String cmd = args[0].toLowerCase();
            if ("--dump".equals(cmd) || "dump".equals(cmd)) {
                dumpAll();
                return;
            } else if ("--stats".equals(cmd) || "stats".equals(cmd)) {
                viewDatabaseStats();
                return;
            } else if ("--map".equals(cmd) || "map".equals(cmd) || "--allocations".equals(cmd)) {
                viewAllocationMap();
                return;
            } else if ("--table".equals(cmd) && args.length > 1) {
                printTable(args[1]);
                return;
            }
        }
        Scanner sc = new Scanner(System.in);
        start(sc);
    }

    public static void start(Scanner sc) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=======================================================");
            System.out.println("           HOTEL DATABASE INSPECTOR (CLI)              ");
            System.out.println("=======================================================");
            System.out.println("Database URL: " + DatabaseConnection.DB_URL);
            System.out.println("-------------------------------------------------------");
            System.out.println("1. View All Tables & Inputs (Full Database Dump)");
            System.out.println("2. View Specific Table (Customers, Staff, Rooms, etc.)");
            System.out.println("3. Search Records Across All Tables");
            System.out.println("4. View Live Room-to-Reservation Allocation Map");
            System.out.println("5. Execute Custom SQL Query (SELECT / Read-Only)");
            System.out.println("6. Database Statistics & Summary");
            System.out.println("7. Back to Previous Menu");
            System.out.println("=======================================================");

            int choice = readIntChoice(sc);
            switch (choice) {
                case 1:
                    dumpAll();
                    break;
                case 2:
                    handleViewSpecificTable(sc);
                    break;
                case 3:
                    handleSearch(sc);
                    break;
                case 4:
                    viewAllocationMap();
                    break;
                case 5:
                    handleCustomQuery(sc);
                    break;
                case 6:
                    viewDatabaseStats();
                    break;
                case 7:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1-7.");
                    break;
            }
        }
    }

    public static void dumpAll() {
        System.out.println("\n================================================================================");
        System.out.println("                     FULL DATABASE DUMP — ALL INPUTS & RECORDS                  ");
        System.out.println("================================================================================");
        for (String table : KNOWN_TABLES) {
            printTable(table);
        }
        System.out.println("\n--- End of Database Dump ---");
    }

    private static void handleViewSpecificTable(Scanner sc) {
        System.out.println("\nSelect a table to view:");
        for (int i = 0; i < KNOWN_TABLES.length; i++) {
            System.out.println((i + 1) + ". " + KNOWN_TABLES[i]);
        }
        System.out.println((KNOWN_TABLES.length + 1) + ". Cancel");

        int choice = readIntChoice(sc);
        if (choice >= 1 && choice <= KNOWN_TABLES.length) {
            printTable(KNOWN_TABLES[choice - 1]);
        } else if (choice == KNOWN_TABLES.length + 1) {
            System.out.println("Cancelled.");
        } else {
            System.out.println("Invalid table selection.");
        }
    }

    public static void printTable(String tableName) {
        String sql = "SELECT * FROM " + tableName;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            renderResultSet(rs, "TABLE: " + tableName.toUpperCase());
        } catch (SQLException e) {
            System.err.println("Error reading table '" + tableName + "': " + e.getMessage());
        }
    }

    private static void handleSearch(Scanner sc) {
        System.out.print("\nEnter keyword/ID to search across all tables: ");
        String query = sc.nextLine().trim();
        if (query.isEmpty()) {
            System.out.println("Search query cannot be empty.");
            return;
        }

        System.out.println("\nSearching for '" + query + "' across all database tables...");
        int totalMatches = 0;

        for (String table : KNOWN_TABLES) {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {

                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                List<String[]> matchingRows = new ArrayList<>();

                while (rs.next()) {
                    boolean matched = false;
                    String[] row = new String[colCount];
                    for (int i = 1; i <= colCount; i++) {
                        String val = rs.getString(i);
                        row[i - 1] = val != null ? val : "NULL";
                        if (val != null && val.toLowerCase().contains(query.toLowerCase())) {
                            matched = true;
                        }
                    }
                    if (matched) {
                        matchingRows.add(row);
                    }
                }

                if (!matchingRows.isEmpty()) {
                    totalMatches += matchingRows.size();
                    String[] headers = new String[colCount];
                    for (int i = 1; i <= colCount; i++) {
                        headers[i - 1] = meta.getColumnLabel(i);
                    }
                    renderRows("MATCHES IN: " + table.toUpperCase(), headers, matchingRows);
                }
            } catch (SQLException e) {
                System.err.println("Search error in " + table + ": " + e.getMessage());
            }
        }

        if (totalMatches == 0) {
            System.out.println("No records found matching '" + query + "'.");
        } else {
            System.out.println("Found " + totalMatches + " matching record(s) in total.");
        }
    }

    public static void viewAllocationMap() {
        String sql = "SELECT r.room_id AS 'Room No', r.room_type AS 'Type', r.price AS 'Price (INR)', " +
                     "r.status AS 'Room Status', " +
                     "COALESCE('#' || res.reservation_id, '-') AS 'Reservation ID', " +
                     "COALESCE(res.status, '-') AS 'Booking Status', " +
                     "COALESCE(c.name, '-') AS 'Guest Name', " +
                     "COALESCE(c.phone, '-') AS 'Phone', " +
                     "COALESCE(res.check_in_date, '-') AS 'Check-In', " +
                     "COALESCE(res.check_out_date, '-') AS 'Check-Out', " +
                     "COALESCE(p.payment_status, '-') AS 'Payment Status' " +
                     "FROM rooms r " +
                     "LEFT JOIN reservations res ON r.room_id = res.room_id " +
                     "    AND UPPER(res.status) NOT IN ('CANCELLED', 'COMPLETED', 'CHECKED_OUT') " +
                     "LEFT JOIN customers c ON res.customer_id = c.customer_id " +
                     "LEFT JOIN payments p ON res.reservation_id = p.reservation_id " +
                     "ORDER BY r.room_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            renderResultSet(rs, "LIVE ROOM & RESERVATION ALLOCATION MAP");
        } catch (SQLException e) {
            System.err.println("Error reading allocation map: " + e.getMessage());
        }
    }

    private static void handleCustomQuery(Scanner sc) {
        System.out.println("\n--- Custom SQL Query Console (Read-Only) ---");
        System.out.println("Examples:");
        System.out.println("  SELECT * FROM rooms WHERE status = 'AVAILABLE'");
        System.out.println("  SELECT reservation_id, customer_id, total_amount FROM reservations WHERE total_amount > 5000");
        System.out.println("  SELECT payment_id, method, amount, payment_status FROM payments");
        System.out.print("\nEnter SQL query: ");
        String sql = sc.nextLine().trim();

        if (sql.isEmpty()) {
            System.out.println("Query cannot be empty.");
            return;
        }

        String upper = sql.toUpperCase();
        if (!upper.startsWith("SELECT") && !upper.startsWith("PRAGMA") && !upper.startsWith("EXPLAIN")) {
            System.out.println("Access restricted: Only read-only queries (SELECT / PRAGMA) are allowed in the Inspector.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            renderResultSet(rs, "QUERY RESULTS: " + sql);
        } catch (SQLException e) {
            System.err.println("SQL Execution Error: " + e.getMessage());
        }
    }

    public static void viewDatabaseStats() {
        System.out.println("\n=======================================================");
        System.out.println("             DATABASE STATISTICS & HEALTH              ");
        System.out.println("=======================================================");
        System.out.println("Database URL : " + DatabaseConnection.DB_URL);

        File dbFile = new File("hotel_astra.db");
        if (dbFile.exists()) {
            System.out.printf("File Size    : %.2f KB (%d bytes)%n", dbFile.length() / 1024.0, dbFile.length());
            System.out.println("Absolute Path: " + dbFile.getAbsolutePath());
        }

        System.out.println("-------------------------------------------------------");
        System.out.printf("%-18s | %-12s%n", "Table Name", "Record Count");
        System.out.println("-------------------+-------------");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String table : KNOWN_TABLES) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM " + table)) {
                    int count = rs.next() ? rs.getInt("total") : 0;
                    System.out.printf("%-18s | %-12d%n", table, count);
                }
            }

            System.out.println("-------------------------------------------------------");

            try (ResultSet rs = stmt.executeQuery("SELECT status, COUNT(*) AS cnt FROM rooms GROUP BY status")) {
                System.out.println("Rooms Breakdown:");
                while (rs.next()) {
                    System.out.printf("  * %-12s: %d room(s)%n", rs.getString("status"), rs.getInt("cnt"));
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total, COALESCE(SUM(total_amount), 0.0) AS sum_rev FROM reservations")) {
                if (rs.next()) {
                    System.out.printf("Total Bookings   : %d%n", rs.getInt("total"));
                    System.out.printf("Total Booking Rev: Rs. %.2f%n", rs.getDouble("sum_rev"));
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(amount), 0.0) AS paid_rev FROM payments WHERE UPPER(payment_status) IN ('PAID', 'COMPLETED')")) {
                if (rs.next()) {
                    System.out.printf("Realized Revenue : Rs. %.2f%n", rs.getDouble("paid_rev"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error reading database stats: " + e.getMessage());
        }
        System.out.println("=======================================================");
    }

    private static void renderResultSet(ResultSet rs, String title) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        String[] headers = new String[colCount];
        for (int i = 1; i <= colCount; i++) {
            headers[i - 1] = meta.getColumnLabel(i);
        }

        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[colCount];
            for (int i = 1; i <= colCount; i++) {
                String val = rs.getString(i);
                row[i - 1] = val != null ? val : "NULL";
            }
            rows.add(row);
        }

        renderRows(title, headers, rows);
    }

    private static void renderRows(String title, String[] headers, List<String[]> rows) {
        int colCount = headers.length;
        int[] widths = new int[colCount];

        for (int i = 0; i < colCount; i++) {
            widths[i] = Math.max(headers[i].length(), 4);
        }

        for (String[] row : rows) {
            for (int i = 0; i < colCount; i++) {
                String cell = row[i];
                if (cell.length() > 45) {
                    cell = cell.substring(0, 42) + "...";
                    row[i] = cell;
                }
                if (cell.length() > widths[i]) {
                    widths[i] = cell.length();
                }
            }
        }

        StringBuilder border = new StringBuilder("+");
        for (int w : widths) {
            char[] dashes = new char[w + 2];
            Arrays.fill(dashes, '-');
            border.append(new String(dashes)).append("+");
        }
        String borderLine = border.toString();

        System.out.println("\n>>> " + title + " <<<");
        System.out.println(borderLine);

        StringBuilder headerRow = new StringBuilder("|");
        for (int i = 0; i < colCount; i++) {
            headerRow.append(String.format(" %-" + widths[i] + "s |", headers[i]));
        }
        System.out.println(headerRow);
        System.out.println(borderLine);

        if (rows.isEmpty()) {
            StringBuilder emptyRow = new StringBuilder("|");
            int totalInnerWidth = borderLine.length() - 2;
            String msg = "(0 records / table is empty)";
            int pad = Math.max(0, (totalInnerWidth - msg.length()) / 2);
            emptyRow.append(" ".repeat(pad)).append(msg);
            int rem = Math.max(0, totalInnerWidth - emptyRow.length() + 1);
            emptyRow.append(" ".repeat(rem)).append("|");
            System.out.println(emptyRow);
        } else {
            for (String[] row : rows) {
                StringBuilder dataRow = new StringBuilder("|");
                for (int i = 0; i < colCount; i++) {
                    dataRow.append(String.format(" %-" + widths[i] + "s |", row[i]));
                }
                System.out.println(dataRow);
            }
        }

        System.out.println(borderLine);
        System.out.println("(" + rows.size() + " row" + (rows.size() == 1 ? "" : "s") + " displayed)\n");
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
