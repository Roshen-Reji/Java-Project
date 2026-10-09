package com.hotel.repository;

import com.hotel.domain.Reservation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationRepository {
    private Map<Integer, Reservation> store = new HashMap<>();
    private int nextId = 1;

    public int add(Reservation r) {
        r.setReservationId(nextId);
        store.put(nextId, r);
        return nextId++;
    }

    public Reservation getById(int id) {
        return store.get(id);
    }

    public List<Reservation> getAll() {
        return new ArrayList<>(store.values());
    }

    public void update(Reservation r) {
        store.put(r.getReservationId(), r);
    }

    public void delete(int id) {
        store.remove(id);
    }

    public List<Reservation> findByCustomerId(int customerId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : store.values()) {
            if (r.getCustomer() != null && r.getCustomer().getCustomerId() == customerId) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Reservation> findByRoomNo(int roomNo) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : store.values()) {
            if (r.getRoom() != null && r.getRoom().getRoomNo() == roomNo) {
                result.add(r);
            }
        }
        return result;
    }
}
