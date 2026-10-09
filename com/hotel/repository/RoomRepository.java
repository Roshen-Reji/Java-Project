package com.hotel.repository;

import com.hotel.domain.Room;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomRepository {
    private Map<Integer, Room> store = new HashMap<>();
    private int nextId = 1;

    public int add(Room r) {
        store.put(r.getRoomNo(), r);
        return r.getRoomNo();
    }

    public Room getById(int id) {
        return store.get(id);
    }

    public List<Room> getAll() {
        return new ArrayList<>(store.values());
    }

    public void update(Room r) {
        store.put(r.getRoomNo(), r);
    }

    public void delete(int id) {
        store.remove(id);
    }

    public List<Room> findAvailableRooms() {
        List<Room> result = new ArrayList<>();
        for (Room r : store.values()) {
            if (r.checkAvailability()) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Room> findByType(String roomType) {
        List<Room> result = new ArrayList<>();
        if (roomType == null) {
            return result;
        }
        for (Room r : store.values()) {
            if (roomType.equalsIgnoreCase(r.getRoomType())) {
                result.add(r);
            }
        }
        return result;
    }
}
