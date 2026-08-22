package com.yogendra.reservation_system.service.impl;

import com.yogendra.reservation_system.dto.RoomRequest;
import com.yogendra.reservation_system.dto.RoomResponse;
import com.yogendra.reservation_system.exception.InvalidReservationDateException;
import com.yogendra.reservation_system.exception.RoomNotFoundException;
import com.yogendra.reservation_system.model.Reservation;
import com.yogendra.reservation_system.model.ReservationStatus;
import com.yogendra.reservation_system.model.Room;
import com.yogendra.reservation_system.repository.ReservationRepository;
import com.yogendra.reservation_system.repository.RoomRepository;
import com.yogendra.reservation_system.service.RoomService;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;


    public RoomServiceImpl(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository
    ) {

        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }


    @Override
    public RoomResponse createRoom(
            RoomRequest roomRequest
    ) {

        Room room = new Room();

        room.setRoomNumber(
                roomRequest.getRoomNumber()
        );

        room.setRoomType(
                roomRequest.getRoomType()
        );

        room.setAvailable(
                roomRequest.isAvailable()
        );

        Room savedRoom =
                roomRepository.save(room);

        return mapToResponse(savedRoom);
    }


    @Override
    public List<RoomResponse> getAllRooms() {

        return roomRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public RoomResponse getRoomById(
            Long id
    ) {

        Room room =
                roomRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RoomNotFoundException(id)
                        );

        return mapToResponse(room);
    }


    @Override
    public List<RoomResponse> getRoomsByType(
            String roomType
    ) {

        return roomRepository
                .findByRoomTypeIgnoreCase(roomType)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public RoomResponse updateRoom(
            Long id,
            RoomRequest roomRequest
    ) {

        Room existingRoom =
                roomRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RoomNotFoundException(id)
                        );

        existingRoom.setRoomNumber(
                roomRequest.getRoomNumber()
        );

        existingRoom.setRoomType(
                roomRequest.getRoomType()
        );

        existingRoom.setAvailable(
                roomRequest.isAvailable()
        );

        Room updatedRoom =
                roomRepository.save(existingRoom);

        return mapToResponse(updatedRoom);
    }


    @Override
    public void deleteRoom(
            Long id
    ) {

        Room existingRoom =
                roomRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RoomNotFoundException(id)
                        );

        roomRepository.delete(existingRoom);
    }


    @Override
    public List<RoomResponse> getAvailableRooms(
            String roomType,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {

        if (!checkOutDate.isAfter(checkInDate)) {

            throw new InvalidReservationDateException(
                    "Check-out date must be after check-in date"
            );
        }

        List<ReservationStatus> activeStatuses =
                List.of(
                        ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED
                );

        List<Room> rooms =
                roomRepository
                        .findByRoomTypeIgnoreCase(
                                roomType
                        );

        if (rooms.isEmpty()) {
            return List.of();
        }

        List<Long> roomIds =
                rooms.stream()
                        .map(Room::getId)
                        .toList();

        List<Reservation> overlappingReservations =
                reservationRepository
                        .findByRoom_IdInAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                roomIds,
                                activeStatuses,
                                checkOutDate,
                                checkInDate
                        );

        Set<Long> bookedRoomIds =
                overlappingReservations
                        .stream()
                        .filter(
                                reservation ->
                                        reservation.getRoom() != null
                        )
                        .map(
                                reservation ->
                                        reservation
                                                .getRoom()
                                                .getId()
                        )
                        .collect(
                                Collectors.toSet()
                        );

        return rooms
                .stream()
                .filter(Room::isAvailable)
                .filter(
                        room ->
                                !bookedRoomIds.contains(
                                        room.getId()
                                )
                )
                .map(this::mapToResponse)
                .toList();
    }


    private RoomResponse mapToResponse(
            Room room
    ) {

        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.isAvailable()
        );
    }
}