package com.hotel.service;

import com.hotel.domain.DeluxeRoom;
import com.hotel.domain.Reservation;
import com.hotel.domain.Room;
import com.hotel.domain.StandardRoom;
import com.hotel.domain.SuiteRoom;
import com.hotel.repository.RoomRepository;
import java.util.List;

public class RoomManagementService {
    private static final RoomRepository roomRepository = new RoomRepository();

    public static RoomRepository getRoomRepository() {
        return roomRepository;
    }

    public static Room addRoom(int roomNo, String roomType, double price, String status, String description) {
        if (roomNo <= 0) {
            System.out.println("Error: Room number must be greater than zero.");
            return null;
        }
        if (price <= 0) {
            System.out.println("Error: Nightly price must be greater than zero.");
            return null;
        }
        if (roomRepository.getById(roomNo) != null) {
            System.out.println("Error: Room " + roomNo + " already exists.");
            return null;
        }
        Room r;
        if ("Deluxe".equalsIgnoreCase(roomType)) {
            r = new DeluxeRoom(roomNo, price, status, description);
        } else if ("Suite".equalsIgnoreCase(roomType)) {
            r = new SuiteRoom(roomNo, price, status, description);
        } else {
            r = new StandardRoom(roomNo, price, status, description);
        }
        roomRepository.add(r);
        return r;
    }

    public static boolean deleteRoom(int roomNo) {
        if (roomRepository.getById(roomNo) == null) {
            System.out.println("Error: Room " + roomNo + " not found.");
            return false;
        }
        List<Reservation> resList = ReservationService.getReservationRepository().findByRoomNo(roomNo);
        for (Reservation r : resList) {
            if ("Confirmed".equalsIgnoreCase(r.getStatus()) || "Pending Approval".equalsIgnoreCase(r.getStatus()) || "Pending".equalsIgnoreCase(r.getStatus())) {
                System.out.println("Error: Cannot delete Room " + roomNo + " because it has an active/pending reservation (#" + r.getReservationId() + ").");
                return false;
            }
        }
        roomRepository.delete(roomNo);
        System.out.println("Room " + roomNo + " deleted successfully.");
        return true;
    }

    public static Room updateRoomDetails(int roomNo, String roomType, double price, String status, String description) {
        Room r = roomRepository.getById(roomNo);
        if (r == null) {
            System.out.println("Error: Room " + roomNo + " not found.");
            return null;
        }
        r.setRoomType(roomType);
        r.setPrice(price);
        r.setStatus(status);
        r.setDescription(description);
        roomRepository.update(r);
        return r;
    }

    public static Room findRoom(int roomNo) {
        return roomRepository.getById(roomNo);
    }

    public static void updateStatus(int roomNo, String status) {
        Room r = roomRepository.getById(roomNo);
        if (r == null) {
            System.out.println("Error: Room " + roomNo + " not found.");
            return;
        }
        r.updateStatus(status);
        roomRepository.update(r);
    }

    public static List<Room> listAvailableRooms() {
        return roomRepository.findAvailableRooms();
    }

    public static List<Room> listAllRooms() {
        return roomRepository.getAll();
    }
}
