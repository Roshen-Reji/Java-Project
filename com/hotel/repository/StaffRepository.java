package com.hotel.repository;

import com.hotel.domain.HotelStaff;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffRepository {
    private Map<Integer, HotelStaff> store = new HashMap<>();
    private int nextId = 1;

    public int add(HotelStaff s) {
        if (s.getStaffId() <= 0) {
            s.setStaffId(nextId);
        }
        store.put(s.getStaffId(), s);
        if (s.getStaffId() >= nextId) {
            nextId = s.getStaffId() + 1;
        }
        return s.getStaffId();
    }

    public HotelStaff getById(int id) {
        return store.get(id);
    }

    public List<HotelStaff> getAll() {
        return new ArrayList<>(store.values());
    }

    public void update(HotelStaff s) {
        store.put(s.getStaffId(), s);
    }

    public void delete(int id) {
        store.remove(id);
    }

    public HotelStaff findByEmail(String email) {
        if (email == null) {
            return null;
        }
        String target = email.trim();
        for (HotelStaff s : store.values()) {
            if (s.getEmail() != null && target.equalsIgnoreCase(s.getEmail().trim())) {
                return s;
            }
        }
        return null;
    }
}
