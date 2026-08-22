package com.yogendra.reservation_system.repository;

import com.yogendra.reservation_system.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDate;
import com.yogendra.reservation_system.model.ReservationStatus;
public interface ReservationRepository
        extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByCustomerNameContainingIgnoreCase(String customerName);
    List<Reservation> findByRoomTypeContainingIgnoreCase(String roomType);
    List<Reservation> findByUser_Username(String username);
    List<Reservation>
    findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
            Long roomId,
            List<ReservationStatus> statuses,
            LocalDate checkOutDate,
            LocalDate checkInDate
    );
    List<Reservation>
    findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
            Long roomId,
            List<ReservationStatus> statuses,
            LocalDate checkOutDate,
            LocalDate checkInDate,
            Long reservationId
    );
    List<Reservation>
    findByRoom_IdInAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
            List<Long> roomIds,
            List<ReservationStatus> statuses,
            LocalDate checkOutDate,
            LocalDate checkInDate
    );
    List<Reservation>
    findByCustomerNameContainingIgnoreCaseAndRoomTypeContainingIgnoreCase(
            String customerName,
            String roomType
    );
    List<Reservation>
    findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
            String roomType,
            LocalDate newCheckOutDate,
            LocalDate newCheckInDate,
            Long id
    );
    List<Reservation>
    findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThan(
            String roomType,
            LocalDate checkOutDate,
            LocalDate checkInDate
    );
    List<Reservation>
    findByRoom_IdAndCheckInDateLessThanAndCheckOutDateGreaterThan(
            Long roomId,
            LocalDate checkOutDate,
            LocalDate checkInDate
    );
    List<Reservation>
    findByRoom_IdAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
            Long roomId,
            LocalDate checkOutDate,
            LocalDate checkInDate,
            Long reservationId
    );
    List<Reservation>
    findByRoom_IdInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
            List<Long> roomIds,
            LocalDate checkOutDate,
            LocalDate checkInDate
    );

}