package com.yogendra.reservation_system.service;

import com.yogendra.reservation_system.dto.RoomRequest;
import com.yogendra.reservation_system.dto.RoomResponse;
import com.yogendra.reservation_system.exception.InvalidReservationDateException;
import com.yogendra.reservation_system.exception.RoomNotFoundException;
import com.yogendra.reservation_system.model.Reservation;
import com.yogendra.reservation_system.model.ReservationStatus;
import com.yogendra.reservation_system.model.Room;
import com.yogendra.reservation_system.repository.ReservationRepository;
import com.yogendra.reservation_system.repository.RoomRepository;
import com.yogendra.reservation_system.service.impl.RoomServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private RoomServiceImpl roomService;


    @Test
    void createRoom_ShouldSaveAndReturnRoom() {

        RoomRequest request =
                new RoomRequest(
                        "201",
                        "Deluxe",
                        true
                );

        Room savedRoom =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        savedRoom.setId(1L);

        when(roomRepository.save(any(Room.class)))
                .thenReturn(savedRoom);

        RoomResponse response =
                roomService.createRoom(request);

        assertEquals(1L, response.getId());
        assertEquals("201", response.getRoomNumber());
        assertEquals("Deluxe", response.getRoomType());
        assertTrue(response.isAvailable());

        verify(roomRepository)
                .save(any(Room.class));
    }


    @Test
    void getAllRooms_ShouldReturnAllRooms() {

        Room room1 =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        room1.setId(1L);

        Room room2 =
                new Room(
                        "301",
                        "Suite",
                        true
                );

        room2.setId(2L);

        when(roomRepository.findAll())
                .thenReturn(
                        List.of(
                                room1,
                                room2
                        )
                );

        List<RoomResponse> response =
                roomService.getAllRooms();

        assertEquals(2, response.size());

        assertEquals(
                "201",
                response.get(0).getRoomNumber()
        );

        assertEquals(
                "Deluxe",
                response.get(0).getRoomType()
        );

        assertEquals(
                "301",
                response.get(1).getRoomNumber()
        );

        assertEquals(
                "Suite",
                response.get(1).getRoomType()
        );

        verify(roomRepository).findAll();
    }


    @Test
    void getRoomById_ShouldReturnRoom_WhenRoomExists() {

        Room room =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        room.setId(1L);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        RoomResponse response =
                roomService.getRoomById(1L);

        assertEquals(1L, response.getId());
        assertEquals("201", response.getRoomNumber());
        assertEquals("Deluxe", response.getRoomType());
        assertTrue(response.isAvailable());

        verify(roomRepository).findById(1L);
    }


    @Test
    void getRoomById_ShouldThrowException_WhenRoomDoesNotExist() {

        when(roomRepository.findById(999L))
                .thenReturn(Optional.empty());

        RoomNotFoundException exception =
                assertThrows(
                        RoomNotFoundException.class,
                        () -> roomService.getRoomById(999L)
                );

        assertEquals(
                "Room not found with id: 999",
                exception.getMessage()
        );
    }


    @Test
    void getRoomsByType_ShouldReturnMatchingRooms() {

        Room room1 =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        room1.setId(1L);

        Room room2 =
                new Room(
                        "202",
                        "Deluxe",
                        true
                );

        room2.setId(2L);

        when(roomRepository.findByRoomTypeIgnoreCase("deluxe"))
                .thenReturn(
                        List.of(
                                room1,
                                room2
                        )
                );

        List<RoomResponse> response =
                roomService.getRoomsByType("deluxe");

        assertEquals(2, response.size());

        assertEquals(
                "201",
                response.get(0).getRoomNumber()
        );

        assertEquals(
                "202",
                response.get(1).getRoomNumber()
        );

        assertTrue(
                response.stream()
                        .allMatch(
                                room ->
                                        room.getRoomType()
                                                .equalsIgnoreCase("Deluxe")
                        )
        );

        verify(roomRepository)
                .findByRoomTypeIgnoreCase("deluxe");
    }


    @Test
    void updateRoom_ShouldUpdateAndReturnRoom() {

        Room existingRoom =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        existingRoom.setId(1L);

        RoomRequest request =
                new RoomRequest(
                        "201",
                        "Suite",
                        false
                );

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(existingRoom));

        when(roomRepository.save(any(Room.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        RoomResponse response =
                roomService.updateRoom(
                        1L,
                        request
                );

        assertEquals(1L, response.getId());
        assertEquals("201", response.getRoomNumber());
        assertEquals("Suite", response.getRoomType());
        assertEquals(false, response.isAvailable());

        verify(roomRepository).findById(1L);
        verify(roomRepository).save(existingRoom);
    }


    @Test
    void updateRoom_ShouldThrowException_WhenRoomDoesNotExist() {

        RoomRequest request =
                new RoomRequest(
                        "999",
                        "Suite",
                        true
                );

        when(roomRepository.findById(999L))
                .thenReturn(Optional.empty());

        RoomNotFoundException exception =
                assertThrows(
                        RoomNotFoundException.class,
                        () ->
                                roomService.updateRoom(
                                        999L,
                                        request
                                )
                );

        assertEquals(
                "Room not found with id: 999",
                exception.getMessage()
        );
    }


    @Test
    void deleteRoom_ShouldDeleteRoom_WhenRoomExists() {

        Room room =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        room.setId(1L);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        roomService.deleteRoom(1L);

        verify(roomRepository).findById(1L);
        verify(roomRepository).delete(room);
    }


    @Test
    void deleteRoom_ShouldThrowException_WhenRoomDoesNotExist() {

        when(roomRepository.findById(999L))
                .thenReturn(Optional.empty());

        RoomNotFoundException exception =
                assertThrows(
                        RoomNotFoundException.class,
                        () ->
                                roomService.deleteRoom(999L)
                );

        assertEquals(
                "Room not found with id: 999",
                exception.getMessage()
        );

        verify(
                roomRepository,
                never()
        ).delete(any(Room.class));
    }


    @Test
    void getAvailableRooms_ShouldReturnOnlyFreeRooms() {

        Room room201 =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        room201.setId(1L);

        Room room202 =
                new Room(
                        "202",
                        "Deluxe",
                        true
                );

        room202.setId(2L);

        Room room203 =
                new Room(
                        "203",
                        "Deluxe",
                        true
                );

        room203.setId(3L);

        Reservation bookedReservation =
                new Reservation();

        bookedReservation.setId(10L);
        bookedReservation.setRoom(room201);

        bookedReservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        bookedReservation.setCheckInDate(
                LocalDate.of(2026, 8, 20)
        );

        bookedReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 25)
        );

        when(
                roomRepository
                        .findByRoomTypeIgnoreCase(
                                "Deluxe"
                        )
        ).thenReturn(
                List.of(
                        room201,
                        room202,
                        room203
                )
        );

        when(
                reservationRepository
                        .findByRoom_IdInAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                List.of(
                                        1L,
                                        2L,
                                        3L
                                ),
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(
                                        2026,
                                        8,
                                        25
                                ),
                                LocalDate.of(
                                        2026,
                                        8,
                                        20
                                )
                        )
        ).thenReturn(
                List.of(bookedReservation)
        );

        List<RoomResponse> response =
                roomService.getAvailableRooms(
                        "Deluxe",
                        LocalDate.of(
                                2026,
                                8,
                                20
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                25
                        )
                );

        assertEquals(
                2,
                response.size()
        );

        assertTrue(
                response.stream()
                        .anyMatch(
                                room ->
                                        room.getRoomNumber()
                                                .equals("202")
                        )
        );

        assertTrue(
                response.stream()
                        .anyMatch(
                                room ->
                                        room.getRoomNumber()
                                                .equals("203")
                        )
        );

        assertTrue(
                response.stream()
                        .noneMatch(
                                room ->
                                        room.getRoomNumber()
                                                .equals("201")
                        )
        );
    }


    @Test
    void getAvailableRooms_ShouldExcludeRoomsMarkedUnavailable() {

        Room room201 =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        room201.setId(1L);

        Room room202 =
                new Room(
                        "202",
                        "Deluxe",
                        false
                );

        room202.setId(2L);

        Room room203 =
                new Room(
                        "203",
                        "Deluxe",
                        true
                );

        room203.setId(3L);

        when(
                roomRepository
                        .findByRoomTypeIgnoreCase(
                                "Deluxe"
                        )
        ).thenReturn(
                List.of(
                        room201,
                        room202,
                        room203
                )
        );

        when(
                reservationRepository
                        .findByRoom_IdInAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                List.of(
                                        1L,
                                        2L,
                                        3L
                                ),
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(
                                        2026,
                                        8,
                                        25
                                ),
                                LocalDate.of(
                                        2026,
                                        8,
                                        20
                                )
                        )
        ).thenReturn(
                List.of()
        );

        List<RoomResponse> response =
                roomService.getAvailableRooms(
                        "Deluxe",
                        LocalDate.of(
                                2026,
                                8,
                                20
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                25
                        )
                );

        assertEquals(
                2,
                response.size()
        );

        assertTrue(
                response.stream()
                        .anyMatch(
                                room ->
                                        room.getRoomNumber()
                                                .equals("201")
                        )
        );

        assertTrue(
                response.stream()
                        .anyMatch(
                                room ->
                                        room.getRoomNumber()
                                                .equals("203")
                        )
        );

        assertTrue(
                response.stream()
                        .noneMatch(
                                room ->
                                        room.getRoomNumber()
                                                .equals("202")
                        )
        );
    }


    @Test
    void getAvailableRooms_ShouldReject_WhenCheckOutDateIsBeforeCheckInDate() {

        InvalidReservationDateException exception =
                assertThrows(
                        InvalidReservationDateException.class,
                        () ->
                                roomService.getAvailableRooms(
                                        "Deluxe",
                                        LocalDate.of(
                                                2026,
                                                8,
                                                25
                                        ),
                                        LocalDate.of(
                                                2026,
                                                8,
                                                20
                                        )
                                )
                );

        assertEquals(
                "Check-out date must be after check-in date",
                exception.getMessage()
        );

        verifyNoInteractions(roomRepository);
        verifyNoInteractions(reservationRepository);
    }


    @Test
    void getAvailableRooms_ShouldIncludeRoom_WhenOnlyOverlappingReservationIsCancelled() {

        LocalDate checkInDate =
                LocalDate.of(
                        2026,
                        9,
                        10
                );

        LocalDate checkOutDate =
                LocalDate.of(
                        2026,
                        9,
                        15
                );

        Room room =
                new Room(
                        "301",
                        "Deluxe",
                        true
                );

        room.setId(30L);

        when(
                roomRepository
                        .findByRoomTypeIgnoreCase(
                                "Deluxe"
                        )
        ).thenReturn(
                List.of(room)
        );

        /*
         * The repository query only searches active statuses:
         *
         * PENDING
         * CONFIRMED
         *
         * Therefore a CANCELLED reservation is not returned.
         */
        when(
                reservationRepository
                        .findByRoom_IdInAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                List.of(30L),
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                checkOutDate,
                                checkInDate
                        )
        ).thenReturn(
                List.of()
        );

        List<RoomResponse> response =
                roomService.getAvailableRooms(
                        "Deluxe",
                        checkInDate,
                        checkOutDate
                );

        assertEquals(
                1,
                response.size()
        );

        assertEquals(
                30L,
                response.get(0).getId()
        );

        assertEquals(
                "301",
                response.get(0).getRoomNumber()
        );

        assertEquals(
                "Deluxe",
                response.get(0).getRoomType()
        );

        assertTrue(
                response.get(0).isAvailable()
        );

        verify(
                reservationRepository
        ).findByRoom_IdInAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                List.of(30L),
                List.of(
                        ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED
                ),
                checkOutDate,
                checkInDate
        );
    }


    @Test
    void getAvailableRooms_ShouldReturnEmptyList_WhenNoRoomsOfRequestedTypeExist() {

        LocalDate checkInDate =
                LocalDate.of(
                        2026,
                        9,
                        10
                );

        LocalDate checkOutDate =
                LocalDate.of(
                        2026,
                        9,
                        15
                );

        when(
                roomRepository
                        .findByRoomTypeIgnoreCase(
                                "Suite"
                        )
        ).thenReturn(
                List.of()
        );

        List<RoomResponse> response =
                roomService.getAvailableRooms(
                        "Suite",
                        checkInDate,
                        checkOutDate
                );

        assertTrue(response.isEmpty());

        verify(
                roomRepository
        ).findByRoomTypeIgnoreCase(
                "Suite"
        );

        verifyNoInteractions(
                reservationRepository
        );
    }
}