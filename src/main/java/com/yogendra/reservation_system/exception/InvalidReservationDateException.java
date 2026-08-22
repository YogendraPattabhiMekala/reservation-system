package com.yogendra.reservation_system.exception;

public class InvalidReservationDateException extends RuntimeException {

    public InvalidReservationDateException(String message) {
        super(message);
    }
}