package com.hotel.service;

import com.hotel.domain.HotelStaff;
import com.hotel.repository.StaffRepository;
import java.util.List;

public class StaffService {
    private static final StaffRepository staffRepository = new StaffRepository();

    public static StaffRepository getStaffRepository() {
        return staffRepository;
    }

    public static HotelStaff addStaff(HotelStaff staff) {
        if (staff == null) {
            return null;
        }
        if (staffRepository.getById(staff.getStaffId()) != null) {
            System.out.println("Error: Staff ID " + staff.getStaffId() + " already exists.");
            return null;
        }
        staffRepository.add(staff);
        return staff;
    }

    public static boolean deleteStaff(int staffId) {
        if (staffRepository.getById(staffId) == null) {
            System.out.println("Error: Staff ID " + staffId + " not found.");
            return false;
        }
        staffRepository.delete(staffId);
        System.out.println("Staff ID " + staffId + " removed successfully.");
        return true;
    }

    public static HotelStaff findStaff(int staffId) {
        return staffRepository.getById(staffId);
    }

    public static List<HotelStaff> listAllStaff() {
        return staffRepository.getAll();
    }
}
