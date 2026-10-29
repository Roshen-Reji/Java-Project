package com.hotel.repository;

import com.hotel.database.DatabaseConnection;
import com.hotel.domain.Admin;
import com.hotel.domain.HotelStaff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StaffRepository {

    public int add(HotelStaff s) {
        if (s == null) return -1;
        int targetId = s.getStaffId();
        if (targetId <= 0) {
            targetId = getNextStaffId();
            s.setStaffId(targetId);
        }

        String role = s instanceof Admin ? "ADMIN" : (s.getRole() != null ? s.getRole() : "STAFF");

        String sql = "INSERT INTO staff (staff_id, name, role, email, phone, password) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, targetId);
            pstmt.setString(2, s.getName());
            pstmt.setString(3, role);
            pstmt.setString(4, s.getEmail());
            pstmt.setString(5, s.getPhone());
            pstmt.setString(6, s.getPassword() != null ? s.getPassword() : "staff");
            pstmt.executeUpdate();
            return targetId;
        } catch (SQLException e) {
            System.err.println("StaffRepository.add error: " + e.getMessage());
            return -1;
        }
    }

    public HotelStaff getById(int id) {
        String sql = "SELECT * FROM staff WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractStaff(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("StaffRepository.getById error: " + e.getMessage());
        }
        return null;
    }

    public List<HotelStaff> getAll() {
        List<HotelStaff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff ORDER BY staff_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractStaff(rs));
            }
        } catch (SQLException e) {
            System.err.println("StaffRepository.getAll error: " + e.getMessage());
        }
        return list;
    }

    public void update(HotelStaff s) {
        if (s == null) return;
        String role = s instanceof Admin ? "ADMIN" : (s.getRole() != null ? s.getRole() : "STAFF");
        String sql = "UPDATE staff SET name = ?, role = ?, email = ?, phone = ?, password = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getName());
            pstmt.setString(2, role);
            pstmt.setString(3, s.getEmail());
            pstmt.setString(4, s.getPhone());
            pstmt.setString(5, s.getPassword() != null ? s.getPassword() : "staff");
            pstmt.setInt(6, s.getStaffId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("StaffRepository.update error: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM staff WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("StaffRepository.delete error: " + e.getMessage());
        }
    }

    public HotelStaff findByEmail(String email) {
        if (email == null) return null;
        String sql = "SELECT * FROM staff WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractStaff(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("StaffRepository.findByEmail error: " + e.getMessage());
        }
        return null;
    }

    private HotelStaff extractStaff(ResultSet rs) throws SQLException {
        int id = rs.getInt("staff_id");
        String name = rs.getString("name");
        String role = rs.getString("role");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String password = rs.getString("password");

        if (role != null && (role.equalsIgnoreCase("ADMIN") || role.toUpperCase().contains("ADMIN"))) {
            return new Admin(id, name, email, phone, password);
        } else {
            return new HotelStaff(id, name, role != null ? role : "Staff", email, phone, password);
        }
    }

    private int getNextStaffId() {
        String sql = "SELECT COALESCE(MAX(staff_id), 0) + 1 AS next_id FROM staff";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        } catch (SQLException e) {
            System.err.println("StaffRepository.getNextStaffId error: " + e.getMessage());
        }
        return 1;
    }
}
