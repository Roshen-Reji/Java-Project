package com.hotel.domain;

public class StandardRoom extends Room {
    public StandardRoom(int roomNo, double price, String status, String description) {
        super(roomNo, "Standard", price, status, description);
    }
}
