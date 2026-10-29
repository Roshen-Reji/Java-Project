package com.hotel.repository;

import com.hotel.database.DatabaseConnection;
import com.hotel.domain.Customer;
import com.hotel.domain.Payment;
import com.hotel.domain.Reservation;
import com.hotel.domain.Room;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationRepository {

    private final CustomerRepository customerRepository = new CustomerRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();

    private static class RawReservation {
        final int resId;
        final int custId;
        final int roomId;
        final String inStr;
        final String outStr;
        final String status;
        final double total;

        RawReservation(int resId, int custId, int roomId, String inStr, String outStr, String status, double total) {
            this.resId = resId;
            this.custId = custId;
            this.roomId = roomId;
            this.inStr = inStr;
            this.outStr = outStr;
            this.status = status;
            this.total = total;
        }
    }

    public int add(Reservation r) {
        if (r == null) return -1;
        String sql;
        boolean hasExplicitId = r.getReservationId() > 0;
        if (hasExplicitId) {
            sql = "INSERT INTO reservations (reservation_id, customer_id, room_id, check_in_date, check_out_date, status, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO reservations (customer_id, room_id, check_in_date, check_out_date, status, total_amount) VALUES (?, ?, ?, ?, ?, ?)";
        }

        int custId = r.getCustomer() != null ? r.getCustomer().getCustomerId() : 0;
        int roomId = r.getRoom() != null ? r.getRoom().getRoomNo() : 0;
        String inStr = r.getCheckInDate() != null ? r.getCheckInDate().toString() : "";
        String outStr = r.getCheckOutDate() != null ? r.getCheckOutDate().toString() : "";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int idx = 1;
            if (hasExplicitId) {
                pstmt.setInt(idx++, r.getReservationId());
            }
            pstmt.setInt(idx++, custId);
            pstmt.setInt(idx++, roomId);
            pstmt.setString(idx++, inStr);
            pstmt.setString(idx++, outStr);
            pstmt.setString(idx++, r.getStatus());
            pstmt.setDouble(idx++, r.getTotalAmount());

            pstmt.executeUpdate();

            if (!hasExplicitId) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int genId = keys.getInt(1);
                        r.setReservationId(genId);
                        return genId;
                    }
                }
            } else {
                return r.getReservationId();
            }
        } catch (SQLException e) {
            System.err.println("ReservationRepository.add error: " + e.getMessage());
        }
        return -1;
    }

    public Reservation getById(int id) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";
        RawReservation raw = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    raw = new RawReservation(
                            rs.getInt("reservation_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("room_id"),
                            rs.getString("check_in_date"),
                            rs.getString("check_out_date"),
                            rs.getString("status"),
                            rs.getDouble("total_amount")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("ReservationRepository.getById error: " + e.getMessage());
        }
        return raw != null ? buildFromRaw(raw) : null;
    }

    public List<Reservation> getAll() {
        List<RawReservation> rawList = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY reservation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rawList.add(new RawReservation(
                        rs.getInt("reservation_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("room_id"),
                        rs.getString("check_in_date"),
                        rs.getString("check_out_date"),
                        rs.getString("status"),
                        rs.getDouble("total_amount")
                ));
            }
        } catch (SQLException e) {
            System.err.println("ReservationRepository.getAll error: " + e.getMessage());
        }

        List<Reservation> list = new ArrayList<>();
        for (RawReservation raw : rawList) {
            list.add(buildFromRaw(raw));
        }
        return list;
    }

    public void update(Reservation r) {
        if (r == null) return;
        String sql = "UPDATE reservations SET customer_id = ?, room_id = ?, check_in_date = ?, check_out_date = ?, status = ?, total_amount = ? WHERE reservation_id = ?";
        int custId = r.getCustomer() != null ? r.getCustomer().getCustomerId() : 0;
        int roomId = r.getRoom() != null ? r.getRoom().getRoomNo() : 0;
        String inStr = r.getCheckInDate() != null ? r.getCheckInDate().toString() : "";
        String outStr = r.getCheckOutDate() != null ? r.getCheckOutDate().toString() : "";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, custId);
            pstmt.setInt(2, roomId);
            pstmt.setString(3, inStr);
            pstmt.setString(4, outStr);
            pstmt.setString(5, r.getStatus());
            pstmt.setDouble(6, r.getTotalAmount());
            pstmt.setInt(7, r.getReservationId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReservationRepository.update error: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReservationRepository.delete error: " + e.getMessage());
        }
    }

    public List<Reservation> findByCustomerId(int customerId) {
        List<RawReservation> rawList = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE customer_id = ? ORDER BY reservation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rawList.add(new RawReservation(
                            rs.getInt("reservation_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("room_id"),
                            rs.getString("check_in_date"),
                            rs.getString("check_out_date"),
                            rs.getString("status"),
                            rs.getDouble("total_amount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("ReservationRepository.findByCustomerId error: " + e.getMessage());
        }

        List<Reservation> list = new ArrayList<>();
        for (RawReservation raw : rawList) {
            list.add(buildFromRaw(raw));
        }
        return list;
    }

    public List<Reservation> findByRoomNo(int roomNo) {
        List<RawReservation> rawList = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE room_id = ? ORDER BY reservation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rawList.add(new RawReservation(
                            rs.getInt("reservation_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("room_id"),
                            rs.getString("check_in_date"),
                            rs.getString("check_out_date"),
                            rs.getString("status"),
                            rs.getDouble("total_amount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("ReservationRepository.findByRoomNo error: " + e.getMessage());
        }

        List<Reservation> list = new ArrayList<>();
        for (RawReservation raw : rawList) {
            list.add(buildFromRaw(raw));
        }
        return list;
    }

    private Reservation buildFromRaw(RawReservation raw) {
        Customer cust = customerRepository.getById(raw.custId);
        Room room = roomRepository.getById(raw.roomId);

        LocalDate checkIn = null;
        if (raw.inStr != null && !raw.inStr.trim().isEmpty()) {
            try {
                checkIn = LocalDate.parse(raw.inStr.trim());
            } catch (Exception ignored) {}
        }

        LocalDate checkOut = null;
        if (raw.outStr != null && !raw.outStr.trim().isEmpty()) {
            try {
                checkOut = LocalDate.parse(raw.outStr.trim());
            } catch (Exception ignored) {}
        }

        Reservation res = new Reservation(raw.resId, cust, room, checkIn, checkOut, raw.status, raw.total);

        Payment pay = paymentRepository.findByReservationId(raw.resId);
        if (pay != null) {
            pay.setReservation(res);
            res.setPayment(pay);
        }

        return res;
    }
}
