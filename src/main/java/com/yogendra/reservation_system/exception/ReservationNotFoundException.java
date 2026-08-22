package com.yogendra.reservation_system.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(Long id) {
        super("Reservation not found with id: " + id);
    }

    public ReservationNotFoundException(String message) {
        super(message);
    }
}