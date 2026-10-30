package com.hotel.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customers ("
                    + "customer_id INTEGER PRIMARY KEY,"
                    + "name VARCHAR(100) NOT NULL,"
                    + "email VARCHAR(100) UNIQUE NOT NULL,"
                    + "phone VARCHAR(20),"
                    + "address VARCHAR(255),"
                    + "password VARCHAR(100)"
                    + ");");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS staff ("
                    + "staff_id INTEGER PRIMARY KEY,"
                    + "name VARCHAR(100) NOT NULL,"
                    + "role VARCHAR(50) NOT NULL,"
                    + "email VARCHAR(100) UNIQUE,"
                    + "phone VARCHAR(20),"
                    + "password VARCHAR(100)"
                    + ");");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rooms ("
                    + "room_id INTEGER PRIMARY KEY,"
                    + "room_type VARCHAR(50) NOT NULL,"
                    + "price DOUBLE NOT NULL,"
                    + "status VARCHAR(30) NOT NULL,"
                    + "description VARCHAR(255)"
                    + ");");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS reservations ("
                    + "reservation_id INTEGER PRIMARY KEY,"
                    + "customer_id INT NOT NULL,"
                    + "room_id INT NOT NULL,"
                    + "check_in_date VARCHAR(30) NOT NULL,"
                    + "check_out_date VARCHAR(30) NOT NULL,"
                    + "status VARCHAR(30) NOT NULL,"
                    + "total_amount DOUBLE,"
                    + "FOREIGN KEY (customer_id) REFERENCES customers(customer_id),"
                    + "FOREIGN KEY (room_id) REFERENCES rooms(room_id)"
                    + ");");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS payments ("
                    + "payment_id INTEGER PRIMARY KEY,"
                    + "reservation_id INT,"
                    + "amount DOUBLE NOT NULL,"
                    + "payment_method VARCHAR(30) NOT NULL,"
                    + "payment_status VARCHAR(30) NOT NULL,"
                    + "payment_date VARCHAR(30),"
                    + "payment_details VARCHAR(255),"
                    + "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)"
                    + ");");

            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM rooms")) {
            if (rs.next() && rs.getInt("count") == 0) {
                String insertRoom = "INSERT INTO rooms (room_id, room_type, price, status, description) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertRoom)) {

                    addRoomBatch(pstmt, 101, "STANDARD", 50.0, "AVAILABLE", "Cozy standard room with single bed");
                    addRoomBatch(pstmt, 102, "STANDARD", 60.0, "AVAILABLE", "Standard room with twin beds & garden view");

                    addRoomBatch(pstmt, 201, "DELUXE", 90.0, "AVAILABLE", "Sea view deluxe room with king bed");
                    addRoomBatch(pstmt, 202, "DELUXE", 100.0, "AVAILABLE", "City view deluxe room with private balcony");

                    addRoomBatch(pstmt, 301, "SUITE", 180.0, "AVAILABLE", "Presidential luxury suite with jacuzzi");
                    addRoomBatch(pstmt, 302, "SUITE", 220.0, "AVAILABLE", "Executive penthouse suite with skyline view");
                    pstmt.executeBatch();
                }
                System.out.println("Default rooms seeded (6 rooms).");
            }
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM staff")) {
            if (rs.next() && rs.getInt("count") == 0) {
                String insertStaff = "INSERT INTO staff (staff_id, name, role, email, phone, password) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertStaff)) {

                    pstmt.setInt(1, 999);
                    pstmt.setString(2, "Master Admin");
                    pstmt.setString(3, "ADMIN");
                    pstmt.setString(4, "admin@hotel.com");
                    pstmt.setString(5, "999-9999");
                    pstmt.setString(6, "admin");
                    pstmt.addBatch();

                    pstmt.setInt(1, 1);
                    pstmt.setString(2, "Alice Johnson");
                    pstmt.setString(3, "STAFF");
                    pstmt.setString(4, "alice@hotel.com");
                    pstmt.setString(5, "111-1111");
                    pstmt.setString(6, "staff");
                    pstmt.addBatch();

                    pstmt.setInt(1, 2);
                    pstmt.setString(2, "Bob Smith");
                    pstmt.setString(3, "STAFF");
                    pstmt.setString(4, "bob@hotel.com");
                    pstmt.setString(5, "222-2222");
                    pstmt.setString(6, "staff");
                    pstmt.addBatch();

                    pstmt.executeBatch();
                }
                System.out.println("Default staff seeded (1 Admin, 2 Staff).");
            }
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM customers")) {
            if (rs.next() && rs.getInt("count") == 0) {
                String insertCust = "INSERT INTO customers (customer_id, name, email, phone, address, password) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertCust)) {
                    pstmt.setInt(1, 1);
                    pstmt.setString(2, "John Doe");
                    pstmt.setString(3, "john@example.com");
                    pstmt.setString(4, "+1 555-123-4567");
                    pstmt.setString(5, "123 Palm Street, CA");
                    pstmt.setString(6, "password123");
                    pstmt.executeUpdate();
                }
                System.out.println("Default customer seeded.");
            }
        }
    }

    private static void addRoomBatch(PreparedStatement pstmt, int id, String type, double price, String status, String desc) throws SQLException {
        pstmt.setInt(1, id);
        pstmt.setString(2, type);
        pstmt.setDouble(3, price);
        pstmt.setString(4, status);
        pstmt.setString(5, desc);
        pstmt.addBatch();
    }
}
