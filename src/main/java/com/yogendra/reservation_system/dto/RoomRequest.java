package com.yogendra.reservation_system.dto;

import jakarta.validation.constraints.NotBlank;

public class RoomRequest {

    @NotBlank(message = "Room number cannot be blank")
    private String roomNumber;

    @NotBlank(message = "Room type cannot be blank")
    private String roomType;

    private boolean available = true;

    public RoomRequest() {
    }

    public RoomRequest(
            String roomNumber,
            String roomType,
            boolean available
    ) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.available = available;
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