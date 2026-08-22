package com.yogendra.reservation_system.exception;

public class ReservationConflictException extends RuntimeException {


    public ReservationConflictException(String message) {
        super(message);
    }
}