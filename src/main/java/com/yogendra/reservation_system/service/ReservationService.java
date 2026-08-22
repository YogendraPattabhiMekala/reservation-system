package com.yogendra.reservation_system.service;

import com.yogendra.reservation_system.dto.ReservationRequest;
import com.yogendra.reservation_system.dto.ReservationResponse;
import org.springframework.data.domain.Page;
import com.yogendra.reservation_system.model.ReservationStatus;
import java.util.List;

public interface ReservationService {
    List<ReservationResponse> searchByRoomType(String roomType);
    List<ReservationResponse> getReservationsByUsername(
            String username
    );
    List<ReservationResponse> filterReservations(
            String customerName,
            String roomType
    );
    Page<ReservationResponse> dynamicFilter(
            String customerName,
            String roomType,
            int page,
            int size,
            String sortBy,
            String direction
    );

    Page<ReservationResponse> getAllReservations(
            int page,
            int size,
            String sortBy,
            String direction
    );
    List<ReservationResponse> searchByCustomerName(String customerName);

    ReservationResponse getReservationById(Long id);

    ReservationResponse createReservation(
            ReservationRequest reservationRequest
    );

    ReservationResponse updateReservation(
            Long id,
            ReservationRequest reservationRequest
    );
    ReservationResponse updateReservationStatus(
            Long id,
            ReservationStatus status
    );
    ReservationResponse createReservation(
            ReservationRequest reservationRequest,
            String username
    );
    ReservationResponse updateReservation(
            Long id,
            ReservationRequest reservationRequest,
            String username
    );
    void deleteReservation(
            Long id,
            String username
    );
    ReservationResponse cancelReservation(
            Long id,
            String username
    );

    void deleteReservation(Long id);
}