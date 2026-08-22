package com.yogendra.reservation_system.dto;

public class RoomResponse {

    private Long id;
    private String roomNumber;
    private String roomType;
    private boolean available;

    public RoomResponse() {
    }

    public RoomResponse(
            Long id,
            String roomNumber,
            String roomType,
            boolean available
    ) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}