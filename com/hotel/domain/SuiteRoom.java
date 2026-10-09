package com.hotel.domain;

public class SuiteRoom extends Room {
    public SuiteRoom(int roomNo, double price, String status, String description) {
        super(roomNo, "Suite", price, status, description);
    }
}
