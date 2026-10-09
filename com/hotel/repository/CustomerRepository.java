package com.hotel.repository;

import com.hotel.domain.Customer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerRepository {
    private Map<Integer, Customer> store = new HashMap<>();
    private int nextId = 1;

    public int add(Customer c) {
        c.setCustomerId(nextId);
        store.put(nextId, c);
        return nextId++;
    }

    public Customer getById(int id) {
        return store.get(id);
    }

    public List<Customer> getAll() {
        return new ArrayList<>(store.values());
    }

    public void update(Customer c) {
        store.put(c.getCustomerId(), c);
    }

    public void delete(int id) {
        store.remove(id);
    }

    public Customer findByEmail(String email) {
        if (email == null) {
            return null;
        }
        String target = email.trim();
        for (Customer c : store.values()) {
            if (c.getEmail() != null && target.equalsIgnoreCase(c.getEmail().trim())) {
                return c;
            }
        }
        return null;
    }

    public Customer findByPhone(String phone) {
        if (phone == null) {
            return null;
        }
        String target = normalizePhone(phone);
        if (target.isEmpty()) {
            return null;
        }
        for (Customer c : store.values()) {
            if (c.getPhone() != null && target.equals(normalizePhone(c.getPhone()))) {
                return c;
            }
        }
        return null;
    }

    private static String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        String t = phone.trim();
        boolean leadingPlus = t.startsWith("+");
        String digits = t.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return "";
        }
        return leadingPlus ? "+" + digits : digits;
    }
}
