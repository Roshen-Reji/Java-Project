package com.hotel.domain;

public class Room {
    protected int roomNo;
    protected String roomType;
    protected double price;
    protected String status;
    protected String description;

    public Room(int roomNo, String roomType, double price, String status, String description) {
        this.roomNo = roomNo;
        this.roomType = roomType;
        this.price = price;
        this.status = status;
        this.description = description;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean checkAvailability() {
        return !"Maintenance".equalsIgnoreCase(status);
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "[" + roomNo + " " + roomType + " $" + price + " " + status + "]";
    }
}
