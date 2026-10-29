package com.hotel.repository;

import com.hotel.database.DatabaseConnection;
import com.hotel.domain.CardPayment;
import com.hotel.domain.CashPayment;
import com.hotel.domain.Payment;
import com.hotel.domain.Reservation;
import com.hotel.domain.UpiPayment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {

    public int add(Payment p) {
        if (p == null) return -1;
        String sql;
        boolean hasExplicitId = p.getPaymentId() > 0;
        if (hasExplicitId) {
            sql = "INSERT INTO payments (payment_id, reservation_id, amount, payment_method, payment_status, payment_date, payment_details) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO payments (reservation_id, amount, payment_method, payment_status, payment_date, payment_details) VALUES (?, ?, ?, ?, ?, ?)";
        }

        int resId = p.getReservation() != null ? p.getReservation().getReservationId() : 0;
        String details = extractDetailsString(p);
        String dateStr = p.getPaymentDate() != null ? p.getPaymentDate().toString() : null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int idx = 1;
            if (hasExplicitId) {
                pstmt.setInt(idx++, p.getPaymentId());
            }
            pstmt.setInt(idx++, resId);
            pstmt.setDouble(idx++, p.getAmount());
            pstmt.setString(idx++, p.getPaymentMethod());
            pstmt.setString(idx++, p.getPaymentStatus());
            pstmt.setString(idx++, dateStr);
            pstmt.setString(idx++, details);

            pstmt.executeUpdate();

            if (!hasExplicitId) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int genId = keys.getInt(1);
                        p.setPaymentId(genId);
                        return genId;
                    }
                }
            } else {
                return p.getPaymentId();
            }
        } catch (SQLException e) {
            System.err.println("PaymentRepository.add error: " + e.getMessage());
        }
        return -1;
    }

    public Payment getById(int id) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractPayment(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("PaymentRepository.getById error: " + e.getMessage());
        }
        return null;
    }

    public List<Payment> getAll() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("PaymentRepository.getAll error: " + e.getMessage());
        }
        return list;
    }

    public void update(Payment p) {
        if (p == null) return;
        String sql = "UPDATE payments SET amount = ?, payment_method = ?, payment_status = ?, payment_date = ?, payment_details = ? WHERE payment_id = ?";
        String details = extractDetailsString(p);
        String dateStr = p.getPaymentDate() != null ? p.getPaymentDate().toString() : null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, p.getAmount());
            pstmt.setString(2, p.getPaymentMethod());
            pstmt.setString(3, p.getPaymentStatus());
            pstmt.setString(4, dateStr);
            pstmt.setString(5, details);
            pstmt.setInt(6, p.getPaymentId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("PaymentRepository.update error: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("PaymentRepository.delete error: " + e.getMessage());
        }
    }

    public Payment findByReservationId(int reservationId) {
        String sql = "SELECT * FROM payments WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractPayment(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("PaymentRepository.findByReservationId error: " + e.getMessage());
        }
        return null;
    }

    private Payment extractPayment(ResultSet rs) throws SQLException {
        int paymentId = rs.getInt("payment_id");
        int resId = rs.getInt("reservation_id");
        double amount = rs.getDouble("amount");
        String method = rs.getString("payment_method");
        String status = rs.getString("payment_status");
        String dateStr = rs.getString("payment_date");
        String details = rs.getString("payment_details");

        Payment payment;
        if (method != null && method.equalsIgnoreCase("Card")) {
            String cardNum = details;
            String cardHolder = "Card Holder";
            if (details != null && details.contains(";")) {
                String[] parts = details.split(";", 2);
                cardNum = parts[0];
                cardHolder = parts.length > 1 ? parts[1] : "Card Holder";
            }
            payment = new CardPayment(paymentId, amount, cardNum, cardHolder);
        } else if (method != null && method.equalsIgnoreCase("UPI")) {
            payment = new UpiPayment(paymentId, amount, details != null ? details : "N/A");
        } else {
            payment = new CashPayment(paymentId, amount);
        }

        payment.setPaymentStatus(status);
        if (dateStr != null && !dateStr.trim().isEmpty() && !dateStr.equalsIgnoreCase("null")) {
            try {
                payment.setPaymentDate(LocalDate.parse(dateStr.trim()));
            } catch (Exception ignored) {}
        }

        if (resId > 0) {
            // Minimal reservation reference to allow resId lookup
            Reservation res = new Reservation(resId, null, null, null, null, "", amount);
            payment.setReservation(res);
        }
        return payment;
    }

    private String extractDetailsString(Payment p) {
        if (p instanceof CardPayment) {
            CardPayment cp = (CardPayment) p;
            return (cp.getCardNumber() != null ? cp.getCardNumber() : "") + ";" + (cp.getCardHolderName() != null ? cp.getCardHolderName() : "");
        } else if (p instanceof UpiPayment) {
            UpiPayment up = (UpiPayment) p;
            return up.getUpiId() != null ? up.getUpiId() : "";
        } else {
            return "Counter Cash Collection";
        }
    }
}
