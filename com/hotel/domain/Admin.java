package com.hotel.domain;

import com.hotel.service.RoomManagementService;
import com.hotel.service.StaffService;

public class Admin extends HotelStaff {

    public Admin(int id, String name, String email, String phone) {
        super(id, name, "Admin", email, phone);
    }

    public boolean createRoom(int roomNo, String roomType, double price, String status, String description) {
        return RoomManagementService.addRoom(roomNo, roomType, price, status, description) != null;
    }

    public boolean deleteRoom(int roomNo) {
        return RoomManagementService.deleteRoom(roomNo);
    }

    public boolean createStaff(int staffId, String name, String role, String email, String phone) {
        return StaffService.addStaff(new HotelStaff(staffId, name, role, email, phone)) != null;
    }

    public boolean removeStaff(int staffId) {
        return StaffService.deleteStaff(staffId);
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Privileges: Full System Administrator");
    }
}
