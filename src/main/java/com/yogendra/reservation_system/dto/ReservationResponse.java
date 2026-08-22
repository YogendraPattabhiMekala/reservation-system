package com.yogendra.reservation_system.dto;

import com.yogendra.reservation_system.model.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;

    private String customerName;

    private String roomType;

    private ReservationStatus status;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Long roomId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public ReservationResponse() {
    }
    public ReservationResponse(
            Long id,
            String customerName,
            String roomType,
            ReservationStatus status
    ) {

        this.id = id;
        this.customerName = customerName;
        this.roomType = roomType;
        this.status = status;
    }
    public ReservationResponse(
            Long id,
            String customerName,
            String roomType
    ) {

        this.id = id;
        this.customerName = customerName;
        this.roomType = roomType;
    }


    public ReservationResponse(
            Long id,
            String customerName,
            String roomType,
            ReservationStatus status,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {

        this.id = id;
        this.customerName = customerName;
        this.roomType = roomType;
        this.status = status;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }


    public ReservationResponse(
            Long id,
            String customerName,
            String roomType,
            ReservationStatus status,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Long roomId
    ) {

        this.id = id;
        this.customerName = customerName;
        this.roomType = roomType;
        this.status = status;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomId = roomId;
    }


    public ReservationResponse(
            Long id,
            String customerName,
            String roomType,
            ReservationStatus status,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Long roomId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = id;
        this.customerName = customerName;
        this.roomType = roomType;
        this.status = status;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomId = roomId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getCustomerName() {
        return customerName;
    }


    public void setCustomerName(
            String customerName
    ) {

        this.customerName = customerName;
    }


    public String getRoomType() {
        return roomType;
    }


    public void setRoomType(
            String roomType
    ) {

        this.roomType = roomType;
    }


    public ReservationStatus getStatus() {
        return status;
    }


    public void setStatus(
            ReservationStatus status
    ) {

        this.status = status;
    }


    public LocalDate getCheckInDate() {
        return checkInDate;
    }


    public void setCheckInDate(
            LocalDate checkInDate
    ) {

        this.checkInDate = checkInDate;
    }


    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }


    public void setCheckOutDate(
            LocalDate checkOutDate
    ) {

        this.checkOutDate = checkOutDate;
    }


    public Long getRoomId() {
        return roomId;
    }


    public void setRoomId(
            Long roomId
    ) {

        this.roomId = roomId;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(
            LocalDateTime createdAt
    ) {

        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {

        this.updatedAt = updatedAt;
    }
}