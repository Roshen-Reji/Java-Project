package com.hotel.repository;

import com.hotel.domain.Payment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentRepository {
    private Map<Integer, Payment> store = new HashMap<>();
    private int nextId = 1;

    public int add(Payment p) {
        p.setPaymentId(nextId);
        store.put(nextId, p);
        return nextId++;
    }

    public Payment getById(int id) {
        return store.get(id);
    }

    public List<Payment> getAll() {
        return new ArrayList<>(store.values());
    }

    public void update(Payment p) {
        store.put(p.getPaymentId(), p);
    }

    public void delete(int id) {
        store.remove(id);
    }

    public Payment findByReservationId(int reservationId) {
        for (Payment p : store.values()) {
            if (p.getReservation() != null && p.getReservation().getReservationId() == reservationId) {
                return p;
            }
        }
        return null;
    }
}
