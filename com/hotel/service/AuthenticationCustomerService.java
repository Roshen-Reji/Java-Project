package com.hotel.service;

import com.hotel.domain.Customer;
import com.hotel.repository.CustomerRepository;
import java.util.List;

public class AuthenticationCustomerService {
    private static final CustomerRepository customerRepository = new CustomerRepository();

    public static CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String e = email.trim();
        if (e.isEmpty() || e.length() > 100) {
            return false;
        }
        if (!e.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return false;
        }
        if (e.contains("..")) {
            return false;
        }
        return true;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        String t = phone.trim();
        if (t.isEmpty()) {
            return false;
        }
        if (!t.matches("[+0-9()\\-\\. ]+")) {
            return false;
        }
        String digits = t.replaceAll("[^0-9]", "");
        return digits.length() >= 7 && digits.length() <= 15;
    }

    public static String normalizePhone(String phone) {
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

    public static Customer registerCustomer(String name, String phone, String email, String address) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: Name cannot be empty.");
            return null;
        }
        if (!isValidEmail(email)) {
            System.out.println("Error: Invalid email format. Use like name@example.com.");
            return null;
        }
        if (!isValidPhone(phone)) {
            System.out.println("Error: Invalid phone. Use 7-15 digits, e.g. +1 555-123-4567.");
            return null;
        }
        String cleanEmail = email.trim();
        String cleanPhone = phone.trim();
        if (customerRepository.findByEmail(cleanEmail) != null) {
            System.out.println("Error: Email already registered.");
            return null;
        }
        if (customerRepository.findByPhone(cleanPhone) != null) {
            System.out.println("Error: Phone number already registered.");
            return null;
        }
        Customer c = new Customer(0, name.trim(), cleanPhone, cleanEmail, address == null ? "" : address.trim());
        customerRepository.add(c);
        return c;
    }

    public static Customer login(String emailOrPhone) {
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty()) {
            return null;
        }
        String key = emailOrPhone.trim();
        Customer c = customerRepository.findByEmail(key);
        if (c != null) {
            return c;
        }
        return customerRepository.findByPhone(key);
    }

    public static boolean deleteCustomer(int customerId) {
        if (customerRepository.getById(customerId) == null) {
            System.out.println("Error: Customer ID " + customerId + " not found.");
            return false;
        }
        customerRepository.delete(customerId);
        System.out.println("Customer ID " + customerId + " deleted successfully.");
        return true;
    }

    public static List<Customer> listAllCustomers() {
        return customerRepository.getAll();
    }
}
