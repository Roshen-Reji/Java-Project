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
        String targetDigits = phone.replaceAll("[^0-9]", "");
        if (target.isEmpty() && targetDigits.isEmpty()) {
            return null;
        }
        for (Customer c : store.values()) {
            if (c.getPhone() != null) {
                String stored = normalizePhone(c.getPhone());
                String storedDigits = c.getPhone().replaceAll("[^0-9]", "");
                if (target.equals(stored) || targetDigits.equals(storedDigits)
                        || (storedDigits.length() >= 7 && targetDigits.length() >= 7
                        && (storedDigits.endsWith(targetDigits) || targetDigits.endsWith(storedDigits)))) {
                    return c;
                }
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
