package com.yogendra.reservation_system.exception;

public class InvalidReservationStatusException extends RuntimeException {

    public InvalidReservationStatusException(String message) {
        super(message);
    }
}