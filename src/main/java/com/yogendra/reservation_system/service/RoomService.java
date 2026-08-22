package com.yogendra.reservation_system.service;

import com.yogendra.reservation_system.dto.RoomRequest;
import com.yogendra.reservation_system.dto.RoomResponse;
import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    RoomResponse createRoom(
            RoomRequest roomRequest
    );

    List<RoomResponse> getAllRooms();

    RoomResponse getRoomById(
            Long id
    );

    List<RoomResponse> getRoomsByType(
            String roomType
    );

    RoomResponse updateRoom(
            Long id,
            RoomRequest roomRequest
    );
    List<RoomResponse> getAvailableRooms(
            String roomType,
            LocalDate checkInDate,
            LocalDate checkOutDate
    );

    void deleteRoom(
            Long id
    );
}