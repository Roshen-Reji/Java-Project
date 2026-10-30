package com.hotel.repository;

import com.hotel.database.DatabaseConnection;
import com.hotel.domain.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {

    public int add(Customer c) {
        if (c == null) return -1;
        String sql;
        boolean hasExplicitId = c.getCustomerId() > 0;
        if (hasExplicitId) {
            sql = "INSERT INTO customers (customer_id, name, email, phone, address, password) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO customers (name, email, phone, address, password) VALUES (?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int paramIndex = 1;
            if (hasExplicitId) {
                pstmt.setInt(paramIndex++, c.getCustomerId());
            }
            pstmt.setString(paramIndex++, c.getName());
            pstmt.setString(paramIndex++, c.getEmail());
            pstmt.setString(paramIndex++, c.getPhone());
            pstmt.setString(paramIndex++, c.getAddress() != null ? c.getAddress() : "");
            pstmt.setString(paramIndex++, c.getPassword() != null ? c.getPassword() : "");

            pstmt.executeUpdate();

            if (!hasExplicitId) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int genId = keys.getInt(1);
                        c.setCustomerId(genId);
                        return genId;
                    }
                }
            } else {
                return c.getCustomerId();
            }
        } catch (SQLException e) {
            System.err.println("CustomerRepository.add error: " + e.getMessage());
        }
        return -1;
    }

    public Customer getById(int id) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("CustomerRepository.getById error: " + e.getMessage());
        }
        return null;
    }

    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("CustomerRepository.getAll error: " + e.getMessage());
        }
        return list;
    }

    public void update(Customer c) {
        if (c == null) return;
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ?, password = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getEmail());
            pstmt.setString(3, c.getPhone());
            pstmt.setString(4, c.getAddress() != null ? c.getAddress() : "");
            pstmt.setString(5, c.getPassword() != null ? c.getPassword() : "");
            pstmt.setInt(6, c.getCustomerId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("CustomerRepository.update error: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("CustomerRepository.delete error: " + e.getMessage());
        }
    }

    public Customer findByEmail(String email) {
        if (email == null) return null;
        String sql = "SELECT * FROM customers WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("CustomerRepository.findByEmail error: " + e.getMessage());
        }
        return null;
    }

    public Customer findByPhone(String phone) {
        if (phone == null) return null;
        String target = normalizePhone(phone);
        if (target.isEmpty()) return null;

        for (Customer c : getAll()) {
            if (c.getPhone() != null && target.equals(normalizePhone(c.getPhone()))) {
                return c;
            }
        }
        return null;
    }

    private Customer extractCustomer(ResultSet rs) throws SQLException {
        int id = rs.getInt("customer_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        String password = rs.getString("password");
        return new Customer(id, name, phone, email, address, password);
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        String t = phone.trim();
        boolean leadingPlus = t.startsWith("+");
        String digits = t.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return "";
        return leadingPlus ? "+" + digits : digits;
    }
}
