package com.yogendra.reservation_system.service;

import com.yogendra.reservation_system.dto.ReservationResponse;
import com.yogendra.reservation_system.model.Reservation;
import com.yogendra.reservation_system.repository.ReservationRepository;
import com.yogendra.reservation_system.service.impl.ReservationServiceImpl;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.yogendra.reservation_system.exception.InvalidReservationStatusException;
import com.yogendra.reservation_system.model.ReservationStatus;
import com.yogendra.reservation_system.dto.ReservationRequest;
import com.yogendra.reservation_system.exception.InvalidReservationDateException;
import java.time.LocalDate;
import com.yogendra.reservation_system.exception.ReservationConflictException;
import com.yogendra.reservation_system.exception.RoomNotFoundException;
import com.yogendra.reservation_system.model.Room;
import com.yogendra.reservation_system.repository.RoomRepository;
import static org.mockito.Mockito.never;
import com.yogendra.reservation_system.entity.User;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.access.AccessDeniedException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import com.yogendra.reservation_system.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {
    @Test
    void dynamicFilter_ShouldFilterByCustomerName() {

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCustomerName("David");
        reservation.setRoomType("Deluxe");

        Page<Reservation> reservationPage =
                new PageImpl<>(List.of(reservation));

        when(reservationRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(reservationPage);

        Page<ReservationResponse> response =
                reservationService.dynamicFilter(
                        "David",
                        null,
                        0,
                        5,
                        "id",
                        "asc"
                );

        assertEquals(1, response.getTotalElements());
        assertEquals(
                "David",
                response.getContent().get(0).getCustomerName()
        );
    }
    @Test
    void dynamicFilter_ShouldFilterByRoomType() {

        Reservation reservation = new Reservation();
        reservation.setId(2L);
        reservation.setCustomerName("Roy");
        reservation.setRoomType("Suite");

        Page<Reservation> reservationPage =
                new PageImpl<>(List.of(reservation));

        when(reservationRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(reservationPage);

        Page<ReservationResponse> response =
                reservationService.dynamicFilter(
                        null,
                        "Suite",
                        0,
                        5,
                        "id",
                        "asc"
                );

        assertEquals(1, response.getTotalElements());
        assertEquals(
                "Suite",
                response.getContent().get(0).getRoomType()
        );
    }
    @Test
    void dynamicFilter_ShouldFilterByCustomerNameAndRoomType() {

        Reservation reservation = new Reservation();
        reservation.setId(3L);
        reservation.setCustomerName("John");
        reservation.setRoomType("Deluxe");

        Page<Reservation> reservationPage =
                new PageImpl<>(List.of(reservation));

        when(reservationRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(reservationPage);

        Page<ReservationResponse> response =
                reservationService.dynamicFilter(
                        "John",
                        "Deluxe",
                        0,
                        5,
                        "id",
                        "asc"
                );

        assertEquals(1, response.getTotalElements());

        assertEquals(
                "John",
                response.getContent().get(0).getCustomerName()
        );

        assertEquals(
                "Deluxe",
                response.getContent().get(0).getRoomType()
        );
    }
    @Test
    void updateReservationStatus_ShouldChangePendingToConfirmed() {

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCustomerName("David");
        reservation.setRoomType("Deluxe");
        reservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response =
                reservationService.updateReservationStatus(
                        1L,
                        ReservationStatus.CONFIRMED
                );

        assertEquals(
                ReservationStatus.CONFIRMED,
                response.getStatus()
        );

        verify(reservationRepository).save(reservation);
    }
    @Test
    void updateReservationStatus_ShouldThrowException_WhenCancelledReservationIsChanged() {

        Reservation reservation = new Reservation();
        reservation.setId(8L);
        reservation.setCustomerName("John");
        reservation.setRoomType("Deluxe");
        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(8L))
                .thenReturn(Optional.of(reservation));

        InvalidReservationStatusException exception =
                assertThrows(
                        InvalidReservationStatusException.class,
                        () -> reservationService.updateReservationStatus(
                                8L,
                                ReservationStatus.CONFIRMED
                        )
                );

        assertEquals(
                "Cancelled reservation status cannot be changed",
                exception.getMessage()
        );
    }
    @Test
    void updateReservationStatus_ShouldThrowException_WhenConfirmedChangedToPending() {

        Reservation reservation = new Reservation();
        reservation.setId(2L);
        reservation.setCustomerName("David");
        reservation.setRoomType("Deluxe");
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(2L))
                .thenReturn(Optional.of(reservation));

        InvalidReservationStatusException exception =
                assertThrows(
                        InvalidReservationStatusException.class,
                        () -> reservationService.updateReservationStatus(
                                2L,
                                ReservationStatus.PENDING
                        )
                );

        assertEquals(
                "Confirmed reservation cannot be changed back to PENDING",
                exception.getMessage()
        );
    }
    @Test
    void updateReservationStatus_ShouldTreatNullStatusAsPending() {

        Reservation reservation = new Reservation();
        reservation.setId(3L);
        reservation.setCustomerName("Roy");
        reservation.setRoomType("Suite");
        reservation.setStatus(null);

        when(reservationRepository.findById(3L))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response =
                reservationService.updateReservationStatus(
                        3L,
                        ReservationStatus.CONFIRMED
                );

        assertEquals(
                ReservationStatus.CONFIRMED,
                response.getStatus()
        );

        verify(reservationRepository).save(reservation);
    }
    @Test
    void createReservation_ShouldReject_WhenCheckOutDateIsBeforeCheckInDate() {

        ReservationRequest request = new ReservationRequest();

        request.setCustomerName("David");
        request.setRoomType("Deluxe");
        request.setCheckInDate(LocalDate.of(2026, 8, 20));
        request.setCheckOutDate(LocalDate.of(2026, 8, 15));

        InvalidReservationDateException exception =
                assertThrows(
                        InvalidReservationDateException.class,
                        () -> reservationService.createReservation(request)
                );

        assertEquals(
                "Check-out date must be after check-in date",
                exception.getMessage()
        );
    }
    @Test
    void createReservation_ShouldSave_WhenDatesAreValid() {

        ReservationRequest request = new ReservationRequest();

        request.setCustomerName("David");
        request.setRoomType("Deluxe");
        request.setCheckInDate(LocalDate.of(2026, 8, 20));
        request.setCheckOutDate(LocalDate.of(2026, 8, 25));

        Reservation savedReservation = new Reservation();
        savedReservation.setId(1L);
        savedReservation.setCustomerName("David");
        savedReservation.setRoomType("Deluxe");
        savedReservation.setStatus(ReservationStatus.PENDING);
        savedReservation.setCheckInDate(LocalDate.of(2026, 8, 20));
        savedReservation.setCheckOutDate(LocalDate.of(2026, 8, 25));

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(savedReservation);

        ReservationResponse response =
                reservationService.createReservation(request);

        assertEquals(1L, response.getId());
        assertEquals("David", response.getCustomerName());
        assertEquals("Deluxe", response.getRoomType());
        assertEquals(ReservationStatus.PENDING, response.getStatus());
        assertEquals(
                LocalDate.of(2026, 8, 20),
                response.getCheckInDate()
        );
        assertEquals(
                LocalDate.of(2026, 8, 25),
                response.getCheckOutDate()
        );

        verify(reservationRepository).save(any(Reservation.class));
    }
    @Test
    void createReservation_ShouldReject_WhenCheckOutDateEqualsCheckInDate() {

        ReservationRequest request = new ReservationRequest();

        request.setCustomerName("David");
        request.setRoomType("Deluxe");
        request.setCheckInDate(LocalDate.of(2026, 8, 20));
        request.setCheckOutDate(LocalDate.of(2026, 8, 20));

        InvalidReservationDateException exception =
                assertThrows(
                        InvalidReservationDateException.class,
                        () -> reservationService.createReservation(request)
                );

        assertEquals(
                "Check-out date must be after check-in date",
                exception.getMessage()
        );
    }
    @Test
    void createReservation_ShouldReject_WhenReservationOverlaps() {

        ReservationRequest request = new ReservationRequest();
        request.setCustomerName("John");
        request.setRoomType("Deluxe");
        request.setCheckInDate(LocalDate.of(2026, 8, 22));
        request.setCheckOutDate(LocalDate.of(2026, 8, 27));

        Reservation existingReservation = new Reservation();
        existingReservation.setId(1L);
        existingReservation.setCustomerName("David");
        existingReservation.setRoomType("Deluxe");
        existingReservation.setCheckInDate(LocalDate.of(2026, 8, 20));
        existingReservation.setCheckOutDate(LocalDate.of(2026, 8, 25));

        when(
                reservationRepository
                        .findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                "Deluxe",
                                LocalDate.of(2026, 8, 27),
                                LocalDate.of(2026, 8, 22)
                        )
        ).thenReturn(List.of(existingReservation));

        ReservationConflictException exception =
                assertThrows(
                        ReservationConflictException.class,
                        () -> reservationService.createReservation(request)
                );

        assertEquals(
                "Reservation overlaps with an existing booking",
                exception.getMessage()
        );

        verify(
                reservationRepository,
                never()
        ).save(any(Reservation.class));
    }
    @Test
    void createReservation_ShouldSave_WhenNoOverlapExists() {

        ReservationRequest request = new ReservationRequest();
        request.setCustomerName("John");
        request.setRoomType("Deluxe");
        request.setCheckInDate(LocalDate.of(2026, 8, 25));
        request.setCheckOutDate(LocalDate.of(2026, 8, 30));

        when(
                reservationRepository
                        .findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                "Deluxe",
                                LocalDate.of(2026, 8, 30),
                                LocalDate.of(2026, 8, 25)
                        )
        ).thenReturn(List.of());

        Reservation savedReservation = new Reservation();
        savedReservation.setId(2L);
        savedReservation.setCustomerName("John");
        savedReservation.setRoomType("Deluxe");
        savedReservation.setStatus(ReservationStatus.PENDING);
        savedReservation.setCheckInDate(LocalDate.of(2026, 8, 25));
        savedReservation.setCheckOutDate(LocalDate.of(2026, 8, 30));

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(savedReservation);

        ReservationResponse response =
                reservationService.createReservation(request);

        assertEquals(2L, response.getId());
        assertEquals("John", response.getCustomerName());
        assertEquals("Deluxe", response.getRoomType());
        assertEquals(
                LocalDate.of(2026, 8, 25),
                response.getCheckInDate()
        );
        assertEquals(
                LocalDate.of(2026, 8, 30),
                response.getCheckOutDate()
        );

        verify(
                reservationRepository
        ).save(any(Reservation.class));
    }
    @Test
    void updateReservation_ShouldSucceed_WhenNoOverlapExists() {

        Long reservationId = 1L;

        Reservation existingReservation = new Reservation();
        existingReservation.setId(reservationId);
        existingReservation.setCustomerName("David");
        existingReservation.setRoomType("Deluxe");
        existingReservation.setCheckInDate(
                LocalDate.of(2026, 8, 20)
        );
        existingReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 25)
        );

        ReservationRequest request =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 26),
                        LocalDate.of(2026, 8, 30)
                );

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(existingReservation));

        when(
                reservationRepository
                        .findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
                                "Deluxe",
                                LocalDate.of(2026, 8, 30),
                                LocalDate.of(2026, 8, 26),
                                reservationId
                        )
        ).thenReturn(List.of());

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response =
                reservationService.updateReservation(
                        reservationId,
                        request
                );

        assertEquals(
                LocalDate.of(2026, 8, 26),
                response.getCheckInDate()
        );

        assertEquals(
                LocalDate.of(2026, 8, 30),
                response.getCheckOutDate()
        );

        verify(
                reservationRepository
        ).save(existingReservation);
    }
    @Test
    void updateReservation_ShouldReject_WhenUpdatedDatesOverlapAnotherReservation() {

        Long reservationId = 1L;

        Reservation existingReservation = new Reservation();
        existingReservation.setId(reservationId);
        existingReservation.setCustomerName("David");
        existingReservation.setRoomType("Deluxe");
        existingReservation.setCheckInDate(
                LocalDate.of(2026, 8, 10)
        );
        existingReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 15)
        );

        Reservation conflictingReservation = new Reservation();
        conflictingReservation.setId(2L);
        conflictingReservation.setCustomerName("John");
        conflictingReservation.setRoomType("Deluxe");
        conflictingReservation.setCheckInDate(
                LocalDate.of(2026, 8, 20)
        );
        conflictingReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 25)
        );
        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(existingReservation));


        ReservationRequest request =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 22),
                        LocalDate.of(2026, 8, 27),
                        10L
                );
        Room room = new Room(
                "201",
                "Deluxe",
                true
        );
        room.setId(10L);
        when(roomRepository.findById(10L))
                .thenReturn(Optional.of(room));

        when(
                reservationRepository
                        .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
                                10L,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(2026, 8, 27),
                                LocalDate.of(2026, 8, 22),
                                1L
                        )
        ).thenReturn(
                List.of(conflictingReservation)
        );

        ReservationConflictException exception =
                assertThrows(
                        ReservationConflictException.class,
                        () -> reservationService.updateReservation(
                                reservationId,
                                request
                        )
                );

        assertEquals(
                "Reservation overlaps with an existing booking",
                exception.getMessage()
        );

        verify(
                reservationRepository,
                never()
        ).save(any(Reservation.class));
    }
    @Test
    void createReservation_ShouldAttachRoom_WhenRoomIdIsValid() {

        Room room = new Room(
                "201",
                "Deluxe",
                true
        );
        room.setId(10L);

        ReservationRequest request =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        10L
                );

        when(
                reservationRepository
                        .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                10L,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(2026, 8, 25),
                                LocalDate.of(2026, 8, 20)
                        )
        ).thenReturn(List.of());

        when(roomRepository.findById(10L))
                .thenReturn(Optional.of(room));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> {

                    Reservation reservation =
                            invocation.getArgument(0);

                    reservation.setId(1L);

                    return reservation;
                });

        ReservationResponse response =
                reservationService.createReservation(request);

        assertEquals(1L, response.getId());
        assertEquals("David", response.getCustomerName());
        assertEquals("Deluxe", response.getRoomType());
        assertEquals(10L, response.getRoomId());

        verify(roomRepository).findById(10L);
        verify(reservationRepository).save(any(Reservation.class));
    }
    @Test
    void createReservation_ShouldThrowRoomNotFound_WhenRoomIdIsInvalid() {

        ReservationRequest request =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        999L
                );



        when(roomRepository.findById(999L))
                .thenReturn(Optional.empty());

        RoomNotFoundException exception =
                assertThrows(
                        RoomNotFoundException.class,
                        () -> reservationService.createReservation(request)
                );

        assertEquals(
                "Room not found with id: 999",
                exception.getMessage()
        );

        verify(
                reservationRepository,
                never()
        ).save(any(Reservation.class));
    }
    @Test
    void createReservation_ShouldReject_WhenSameRoomOverlaps() {

        Room room = new Room(
                "201",
                "Deluxe",
                true
        );
        room.setId(10L);

        ReservationRequest request =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        10L
                );

        Reservation existingReservation = new Reservation();
        existingReservation.setId(2L);
        existingReservation.setCustomerName("John");
        existingReservation.setRoomType("Deluxe");
        existingReservation.setRoom(room);
        existingReservation.setCheckInDate(
                LocalDate.of(2026, 8, 22)
        );
        existingReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 27)
        );

        when(roomRepository.findById(10L))
                .thenReturn(Optional.of(room));

        when(
                reservationRepository
                        .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                10L,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(2026, 8, 25),
                                LocalDate.of(2026, 8, 20)
                        )
        ).thenReturn(
                List.of(existingReservation)
        );

        ReservationConflictException exception =
                assertThrows(
                        ReservationConflictException.class,
                        () -> reservationService.createReservation(request)
                );

        assertEquals(
                "Reservation overlaps with an existing booking",
                exception.getMessage()
        );

        verify(
                reservationRepository,
                never()
        ).save(any(Reservation.class));
    }
    @Test
    void createReservation_ShouldAllowSameDates_WhenRoomsAreDifferent() {

        Room room202 = new Room(
                "202",
                "Deluxe",
                true
        );
        room202.setId(20L);

        ReservationRequest request =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        20L
                );

        when(roomRepository.findById(20L))
                .thenReturn(Optional.of(room202));

        when(
                reservationRepository
                        .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                20L,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(2026, 8, 25),
                                LocalDate.of(2026, 8, 20)
                        )
        ).thenReturn(List.of());

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> {

                    Reservation reservation =
                            invocation.getArgument(0);

                    reservation.setId(3L);

                    return reservation;
                });

        ReservationResponse response =
                reservationService.createReservation(request);

        assertEquals(3L, response.getId());
        assertEquals("John", response.getCustomerName());
        assertEquals("Deluxe", response.getRoomType());
        assertEquals(20L, response.getRoomId());

        verify(roomRepository).findById(20L);
        verify(reservationRepository)
                .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                        20L,
                        List.of(
                                ReservationStatus.PENDING,
                                ReservationStatus.CONFIRMED
                        ),
                        LocalDate.of(2026, 8, 25),
                        LocalDate.of(2026, 8, 20)
                );

        verify(reservationRepository)
                .save(any(Reservation.class));
    }
    @Test
    void updateReservation_ShouldAllowSameDates_WhenRoomsAreDifferent() {

        Long reservationId = 1L;

        Reservation existingReservation = new Reservation();
        existingReservation.setId(reservationId);
        existingReservation.setCustomerName("John");
        existingReservation.setRoomType("Deluxe");
        existingReservation.setCheckInDate(
                LocalDate.of(2026, 8, 10)
        );
        existingReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 15)
        );

        Room room202 = new Room(
                "202",
                "Deluxe",
                true
        );
        room202.setId(20L);

        ReservationRequest request =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        20L
                );

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(existingReservation));

        when(roomRepository.findById(20L))
                .thenReturn(Optional.of(room202));

        when(
                reservationRepository
                        .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
                                20L,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(2026, 8, 25),
                                LocalDate.of(2026, 8, 20),
                                1L
                        )
        ).thenReturn(List.of());

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response =
                reservationService.updateReservation(
                        reservationId,
                        request
                );

        assertEquals("John", response.getCustomerName());
        assertEquals("Deluxe", response.getRoomType());
        assertEquals(20L, response.getRoomId());

        assertEquals(
                LocalDate.of(2026, 8, 20),
                response.getCheckInDate()
        );

        assertEquals(
                LocalDate.of(2026, 8, 25),
                response.getCheckOutDate()
        );

        verify(roomRepository).findById(20L);

        verify(reservationRepository)
                .save(existingReservation);
    }
    @Test
    void getReservationsByUsername_ShouldReturnOnlyUsersReservations() {

        User john = new User();
        john.setUsername("john");

        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setCustomerName("John");
        reservation1.setRoomType("Deluxe");
        reservation1.setUser(john);

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setCustomerName("John");
        reservation2.setRoomType("Suite");
        reservation2.setUser(john);

        when(
                reservationRepository.findByUser_Username("john")
        ).thenReturn(
                List.of(
                        reservation1,
                        reservation2
                )
        );

        List<ReservationResponse> response =
                reservationService.getReservationsByUsername(
                        "john"
                );

        assertEquals(2, response.size());

        assertEquals(
                1L,
                response.get(0).getId()
        );

        assertEquals(
                "Deluxe",
                response.get(0).getRoomType()
        );

        assertEquals(
                2L,
                response.get(1).getId()
        );

        assertEquals(
                "Suite",
                response.get(1).getRoomType()
        );

        verify(
                reservationRepository
        ).findByUser_Username(
                "john"
        );
    }
    @Test
    void createReservation_WithUsername_ShouldAttachUserToReservation() {

        User john = new User();
        john.setUsername("john");

        Room room = new Room(
                "201",
                "Deluxe",
                true
        );
        room.setId(10L);

        ReservationRequest request =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        10L
                );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(john));

        when(roomRepository.findById(10L))
                .thenReturn(Optional.of(room));

        when(
                reservationRepository
                        .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                10L,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(2026, 8, 25),
                                LocalDate.of(2026, 8, 20)
                        )
        ).thenReturn(List.of());

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> {

                    Reservation reservation =
                            invocation.getArgument(0);

                    reservation.setId(1L);

                    return reservation;
                });

        ReservationResponse response =
                reservationService.createReservation(
                        request,
                        "john"
                );

        assertEquals(1L, response.getId());

        verify(userRepository)
                .findByUsername("john");

        verify(reservationRepository)
                .save(argThat(reservation ->
                        reservation.getUser() == john
                                && reservation.getRoom() == room
                                && reservation.getCustomerName().equals("John")
                ));
    }
    @Test
    void updateReservation_WithUsername_ShouldAllowOwnerToUpdate() {

        User john = new User();
        john.setUsername("john");
        john.setRole("USER");

        Reservation existingReservation = new Reservation();
        existingReservation.setId(1L);
        existingReservation.setCustomerName("John");
        existingReservation.setRoomType("Deluxe");
        existingReservation.setCheckInDate(
                LocalDate.of(2026, 8, 20)
        );
        existingReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 25)
        );
        existingReservation.setUser(john);

        ReservationRequest request =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 22),
                        LocalDate.of(2026, 8, 27)
                );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(john));

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(existingReservation));

        when(
                reservationRepository
                        .findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
                                "Deluxe",
                                LocalDate.of(2026, 8, 27),
                                LocalDate.of(2026, 8, 22),
                                1L
                        )
        ).thenReturn(List.of());

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response =
                reservationService.updateReservation(
                        1L,
                        request,
                        "john"
                );

        assertEquals(
                "John",
                response.getCustomerName()
        );

        assertEquals(
                LocalDate.of(2026, 8, 22),
                response.getCheckInDate()
        );

        assertEquals(
                LocalDate.of(2026, 8, 27),
                response.getCheckOutDate()
        );

        verify(userRepository)
                .findByUsername("john");

        verify(reservationRepository)
                .save(existingReservation);
    }
    @Test
    void updateReservation_WithUsername_ShouldReject_WhenUserIsNotOwner() {

        User john = new User();
        john.setUsername("john");
        john.setRole("USER");

        User david = new User();
        david.setUsername("david");
        david.setRole("USER");

        Reservation davidReservation = new Reservation();
        davidReservation.setId(2L);
        davidReservation.setCustomerName("David");
        davidReservation.setRoomType("Deluxe");
        davidReservation.setCheckInDate(
                LocalDate.of(2026, 8, 20)
        );
        davidReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 25)
        );
        davidReservation.setUser(david);

        ReservationRequest request =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 22),
                        LocalDate.of(2026, 8, 27)
                );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(john));

        when(reservationRepository.findById(2L))
                .thenReturn(Optional.of(davidReservation));

        AccessDeniedException exception =
                assertThrows(
                        AccessDeniedException.class,
                        () -> reservationService.updateReservation(
                                2L,
                                request,
                                "john"
                        )
                );

        assertEquals(
                "You are not allowed to update this reservation",
                exception.getMessage()
        );

        verify(
                reservationRepository,
                never()
        ).save(any(Reservation.class));
    }
    @Test
    void deleteReservation_WithUsername_ShouldAllowOwnerToDelete() {

        User john = new User();
        john.setUsername("john");
        john.setRole("USER");

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCustomerName("John");
        reservation.setRoomType("Deluxe");
        reservation.setUser(john);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(john));

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(
                1L,
                "john"
        );

        verify(userRepository)
                .findByUsername("john");

        verify(reservationRepository)
                .findById(1L);

        verify(reservationRepository)
                .delete(reservation);
    }
    @Test
    void deleteReservation_WithUsername_ShouldReject_WhenUserIsNotOwner() {

        User john = new User();
        john.setUsername("john");
        john.setRole("USER");

        User david = new User();
        david.setUsername("david");
        david.setRole("USER");

        Reservation davidReservation = new Reservation();
        davidReservation.setId(2L);
        davidReservation.setCustomerName("David");
        davidReservation.setRoomType("Deluxe");
        davidReservation.setUser(david);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(john));

        when(reservationRepository.findById(2L))
                .thenReturn(Optional.of(davidReservation));

        AccessDeniedException exception =
                assertThrows(
                        AccessDeniedException.class,
                        () -> reservationService.deleteReservation(
                                2L,
                                "john"
                        )
                );

        assertEquals(
                "You are not allowed to delete this reservation",
                exception.getMessage()
        );

        verify(
                reservationRepository,
                never()
        ).delete(any(Reservation.class));
    }
    @Test
    void cancelReservation_WithUsername_ShouldAllowOwnerToCancel() {

        User john = new User();
        john.setUsername("john");
        john.setRole("USER");

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCustomerName("John");
        reservation.setRoomType("Deluxe");
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setUser(john);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(john));

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response =
                reservationService.cancelReservation(
                        1L,
                        "john"
                );

        assertEquals(
                ReservationStatus.CANCELLED,
                response.getStatus()
        );

        assertEquals(
                ReservationStatus.CANCELLED,
                reservation.getStatus()
        );

        verify(userRepository)
                .findByUsername("john");

        verify(reservationRepository)
                .findById(1L);

        verify(reservationRepository)
                .save(reservation);

        verify(
                reservationRepository,
                never()
        ).delete(any(Reservation.class));
    }
    @Test
    void cancelReservation_WithUsername_ShouldReject_WhenUserIsNotOwner() {

        User john = new User();
        john.setUsername("john");
        john.setRole("USER");

        User david = new User();
        david.setUsername("david");
        david.setRole("USER");

        Reservation reservation = new Reservation();
        reservation.setId(2L);
        reservation.setCustomerName("David");
        reservation.setRoomType("Deluxe");
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setUser(david);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(john));

        when(reservationRepository.findById(2L))
                .thenReturn(Optional.of(reservation));

        AccessDeniedException exception =
                assertThrows(
                        AccessDeniedException.class,
                        () -> reservationService.cancelReservation(
                                2L,
                                "john"
                        )
                );

        assertEquals(
                "You are not allowed to cancel this reservation",
                exception.getMessage()
        );

        verify(
                reservationRepository,
                never()
        ).save(any(Reservation.class));
    }
    @Test
    void cancelReservation_ShouldReject_WhenReservationIsAlreadyCancelled() {

        User john = new User();
        john.setUsername("john");
        john.setRole("USER");

        Reservation reservation = new Reservation();
        reservation.setId(3L);
        reservation.setCustomerName("John");
        reservation.setRoomType("Deluxe");
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUser(john);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(john));

        when(reservationRepository.findById(3L))
                .thenReturn(Optional.of(reservation));

        InvalidReservationStatusException exception =
                assertThrows(
                        InvalidReservationStatusException.class,
                        () -> reservationService.cancelReservation(
                                3L,
                                "john"
                        )
                );

        assertEquals(
                "Reservation is already cancelled",
                exception.getMessage()
        );

        verify(
                reservationRepository,
                never()
        ).save(any(Reservation.class));

        verify(
                reservationRepository,
                never()
        ).delete(any(Reservation.class));
    }
    @Test
    void createReservation_ShouldAllowBooking_WhenOverlappingReservationIsCancelled() {

        Room room = new Room(
                "201",
                "Deluxe",
                true
        );
        room.setId(10L);

        ReservationRequest request =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 22),
                        LocalDate.of(2026, 8, 27),
                        10L
                );

        when(roomRepository.findById(10L))
                .thenReturn(Optional.of(room));

        when(
                reservationRepository
                        .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                10L,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(2026, 8, 27),
                                LocalDate.of(2026, 8, 22)
                        )
        ).thenReturn(List.of());

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> {

                    Reservation reservation =
                            invocation.getArgument(0);

                    reservation.setId(3L);

                    return reservation;
                });

        ReservationResponse response =
                reservationService.createReservation(request);

        assertEquals(3L, response.getId());
        assertEquals("John", response.getCustomerName());
        assertEquals(10L, response.getRoomId());

        verify(reservationRepository)
                .save(any(Reservation.class));
    }
    @Test
    void updateReservation_ShouldAllowUpdate_WhenOverlappingReservationIsCancelled() {

        Room room = new Room(
                "301",
                "Deluxe",
                true
        );
        room.setId(30L);

        Reservation existingReservation = new Reservation();
        existingReservation.setId(1L);
        existingReservation.setCustomerName("John");
        existingReservation.setRoomType("Deluxe");
        existingReservation.setRoom(room);
        existingReservation.setCheckInDate(
                LocalDate.of(2026, 9, 10)
        );
        existingReservation.setCheckOutDate(
                LocalDate.of(2026, 9, 15)
        );

        ReservationRequest request =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 9, 20),
                        LocalDate.of(2026, 9, 25),
                        30L
                );

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(existingReservation));

        when(roomRepository.findById(30L))
                .thenReturn(Optional.of(room));

        when(
                reservationRepository
                        .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
                                30L,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                LocalDate.of(2026, 9, 25),
                                LocalDate.of(2026, 9, 20),
                                1L
                        )
        ).thenReturn(List.of());

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        ReservationResponse response =
                reservationService.updateReservation(
                        1L,
                        request
                );

        assertEquals(
                LocalDate.of(2026, 9, 20),
                response.getCheckInDate()
        );

        assertEquals(
                LocalDate.of(2026, 9, 25),
                response.getCheckOutDate()
        );

        assertEquals(
                30L,
                response.getRoomId()
        );

        verify(reservationRepository)
                .save(existingReservation);
    }

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

}