package com.hotel.repository;

import com.hotel.database.DatabaseConnection;
import com.hotel.domain.DeluxeRoom;
import com.hotel.domain.Room;
import com.hotel.domain.StandardRoom;
import com.hotel.domain.SuiteRoom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoomRepository {

    public int add(Room r) {
        if (r == null) return -1;
        int targetId = r.getRoomNo();
        if (targetId <= 0) {
            targetId = getNextRoomNo();
            r.setRoomNo(targetId);
        }

        String sql = "INSERT INTO rooms (room_id, room_type, price, status, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, targetId);
            pstmt.setString(2, r.getRoomType());
            pstmt.setDouble(3, r.getPrice());
            pstmt.setString(4, r.getStatus());
            pstmt.setString(5, r.getDescription() != null ? r.getDescription() : "");
            pstmt.executeUpdate();
            return targetId;
        } catch (SQLException e) {
            System.err.println("RoomRepository.add error: " + e.getMessage());
            return -1;
        }
    }

    public Room getById(int id) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractRoom(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("RoomRepository.getById error: " + e.getMessage());
        }
        return null;
    }

    public Room findByRoomNo(int roomNo) {
        return getById(roomNo);
    }

    public List<Room> getAll() {
        List<Room> result = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(extractRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("RoomRepository.getAll error: " + e.getMessage());
        }
        return result;
    }

    public void update(Room r) {
        if (r == null) return;
        String sql = "UPDATE rooms SET room_type = ?, price = ?, status = ?, description = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, r.getRoomType());
            pstmt.setDouble(2, r.getPrice());
            pstmt.setString(3, r.getStatus());
            pstmt.setString(4, r.getDescription() != null ? r.getDescription() : "");
            pstmt.setInt(5, r.getRoomNo());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("RoomRepository.update error: " + e.getMessage());
        }
    }

    public void updateStatus(int roomNo, String status) {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, roomNo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("RoomRepository.updateStatus error: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("RoomRepository.delete error: " + e.getMessage());
        }
    }

    public List<Room> findAvailableRooms() {
        List<Room> result = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE LOWER(status) = 'available' ORDER BY room_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(extractRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("RoomRepository.findAvailableRooms error: " + e.getMessage());
        }
        return result;
    }

    public List<Room> findByType(String roomType) {
        List<Room> result = new ArrayList<>();
        if (roomType == null) return result;
        String sql = "SELECT * FROM rooms WHERE LOWER(room_type) = LOWER(?) ORDER BY room_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomType.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(extractRoom(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("RoomRepository.findByType error: " + e.getMessage());
        }
        return result;
    }

    private Room extractRoom(ResultSet rs) throws SQLException {
        int roomNo = rs.getInt("room_id");
        String roomType = rs.getString("room_type");
        double price = rs.getDouble("price");
        String status = rs.getString("status");
        String desc = rs.getString("description");

        if (roomType != null && roomType.equalsIgnoreCase("Deluxe")) {
            return new DeluxeRoom(roomNo, price, status, desc);
        } else if (roomType != null && roomType.equalsIgnoreCase("Suite")) {
            return new SuiteRoom(roomNo, price, status, desc);
        } else {
            return new StandardRoom(roomNo, price, status, desc);
        }
    }

    private int getNextRoomNo() {
        String sql = "SELECT COALESCE(MAX(room_id), 100) + 1 AS next_id FROM rooms";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        } catch (SQLException e) {
            System.err.println("RoomRepository.getNextRoomNo error: " + e.getMessage());
        }
        return 101;
    }
}
