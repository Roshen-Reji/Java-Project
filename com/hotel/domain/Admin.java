package com.hotel.domain;

import com.hotel.service.RoomManagementService;
import com.hotel.service.StaffService;

public class Admin extends HotelStaff {

    public Admin(int id, String name, String email, String phone) {
        super(id, name, "Admin", email, phone);
    }

    public Admin(int id, String name, String email, String phone, String password) {
        super(id, name, "Admin", email, phone, password);
    }

    public void createRoom(int roomNo, String roomType, double price, String status, String description) {
        RoomManagementService.addRoom(roomNo, roomType, price, status, description);
    }

    public void deleteRoom(int roomNo) {
        RoomManagementService.deleteRoom(roomNo);
    }

    public void createStaff(int staffId, String name, String role, String email, String phone) {
        StaffService.addStaff(new HotelStaff(staffId, name, role, email, phone));
    }

    public void removeStaff(int staffId) {
        StaffService.deleteStaff(staffId);
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Privileges: Full System Administrator");
    }
}
