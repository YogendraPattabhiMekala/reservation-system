package com.yogendra.reservation_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ReservationRequest {

    private Long roomId;

    @NotBlank(message = "Customer Name cannot be blank")
    @Size(
            min = 3,
            max = 50,
            message = "Customer Name should be between 3 and 50 characters"
    )
    private String customerName;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @NotBlank(message = "Room Type cannot be blank")
    private String roomType;


    public ReservationRequest() {
    }


    public ReservationRequest(
            String customerName,
            String roomType
    ) {
        this.customerName = customerName;
        this.roomType = roomType;
    }


    public ReservationRequest(
            String customerName,
            String roomType,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {
        this.customerName = customerName;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }


    public ReservationRequest(
            String customerName,
            String roomType,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Long roomId
    ) {
        this.customerName = customerName;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomId = roomId;
    }


    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }


    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }


    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }


    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }


    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
}