package com.yogendra.reservation_system.service.impl;

import com.yogendra.reservation_system.dto.ReservationRequest;
import com.yogendra.reservation_system.dto.ReservationResponse;

import com.yogendra.reservation_system.entity.User;

import com.yogendra.reservation_system.exception.InvalidReservationDateException;
import com.yogendra.reservation_system.exception.InvalidReservationStatusException;
import com.yogendra.reservation_system.exception.ReservationConflictException;
import com.yogendra.reservation_system.exception.ReservationNotFoundException;
import com.yogendra.reservation_system.exception.RoomNotFoundException;
import com.yogendra.reservation_system.model.Reservation;
import com.yogendra.reservation_system.model.ReservationStatus;
import com.yogendra.reservation_system.model.Room;
import org.springframework.security.access.AccessDeniedException;
import com.yogendra.reservation_system.repository.ReservationRepository;
import com.yogendra.reservation_system.repository.RoomRepository;
import com.yogendra.reservation_system.repository.UserRepository;

import com.yogendra.reservation_system.service.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger logger =
            LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;


    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            UserRepository userRepository
    ) {

        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }


    @Override
    public Page<ReservationResponse> getAllReservations(
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        logger.info(
                "Fetching reservations - page: {}, size: {}, sortBy: {}, direction: {}",
                page,
                size,
                sortBy,
                direction
        );

        Sort sort =
                direction.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );

        Page<Reservation> reservationPage =
                reservationRepository.findAll(pageable);

        Page<ReservationResponse> responsePage =
                reservationPage.map(this::mapToResponse);

        logger.info(
                "Successfully fetched {} reservations",
                responsePage.getNumberOfElements()
        );

        return responsePage;
    }


    @Override
    public ReservationResponse getReservationById(
            Long id
    ) {

        logger.info(
                "Fetching reservation with id: {}",
                id
        );

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Reservation not found with id: {}",
                                    id
                            );

                            return new ReservationNotFoundException(id);
                        });

        logger.info(
                "Successfully fetched reservation with id: {}",
                id
        );

        return mapToResponse(reservation);
    }


    // Legacy create method.
    // Existing tests and older code can continue using this.
    @Override
    public ReservationResponse createReservation(
            ReservationRequest reservationRequest
    ) {

        return createReservationInternal(
                reservationRequest,
                null
        );
    }


    // Ownership-aware create method.
    @Override
    public ReservationResponse createReservation(
            ReservationRequest reservationRequest,
            String username
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found: " + username
                                )
                        );

        return createReservationInternal(
                reservationRequest,
                user
        );
    }


    /*
     * Shared reservation creation logic.
     *
     * Both old reservation creation and authenticated
     * reservation creation use this method.
     *
     * This prevents:
     *
     * create -> save
     * find again
     * attach user
     * save again
     *
     * Instead, everything is attached before one save.
     */
    private ReservationResponse createReservationInternal(
            ReservationRequest reservationRequest,
            User user
    ) {

        // 1. Validate date range
        if (!reservationRequest.getCheckOutDate()
                .isAfter(reservationRequest.getCheckInDate())) {

            throw new InvalidReservationDateException(
                    "Check-out date must be after check-in date"
            );
        }


        // 2. Resolve actual room when roomId is provided
        Room room = null;

        if (reservationRequest.getRoomId() != null) {

            room =
                    roomRepository.findById(
                                    reservationRequest.getRoomId()
                            )
                            .orElseThrow(
                                    () -> new RoomNotFoundException(
                                            reservationRequest.getRoomId()
                                    )
                            );
        }
        List<ReservationStatus> activeStatuses =
                List.of(
                        ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED
                );


        // 3. Check overlapping reservations
        List<Reservation> overlappingReservations;


        if (room != null) {

            overlappingReservations =
                    reservationRepository
                            .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                    room.getId(),
                                    activeStatuses,
                                    reservationRequest.getCheckOutDate(),
                                    reservationRequest.getCheckInDate()
                            );

        } else {

            /*
             * Temporary legacy support.
             *
             * Older tests/requests may not contain roomId yet,
             * so roomType overlap logic remains available.
             */
            overlappingReservations =
                    reservationRepository
                            .findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                    reservationRequest.getRoomType(),
                                    reservationRequest.getCheckOutDate(),
                                    reservationRequest.getCheckInDate()
                            );
        }

        if (overlappingReservations != null
                && !overlappingReservations.isEmpty()) {

            throw new ReservationConflictException(
                    "Reservation overlaps with an existing booking"
            );
        }


        // 4. Build reservation entity
        logger.info(
                "Creating reservation for customer: {}",
                reservationRequest.getCustomerName()
        );

        Reservation reservation =
                new Reservation();

        reservation.setCustomerName(
                reservationRequest.getCustomerName()
        );

        reservation.setRoomType(
                reservationRequest.getRoomType()
        );

        reservation.setCheckInDate(
                reservationRequest.getCheckInDate()
        );

        reservation.setCheckOutDate(
                reservationRequest.getCheckOutDate()
        );

        reservation.setRoom(room);


        /*
         * Ownership is attached BEFORE saving.
         *
         * Authenticated creation:
         * user != null
         *
         * Legacy creation:
         * user == null
         */
        reservation.setUser(user);


        // 5. Save only once
        Reservation savedReservation =
                reservationRepository.save(reservation);

        logger.info(
                "Reservation created successfully with id: {}",
                savedReservation.getId()
        );

        return mapToResponse(savedReservation);
    }


    @Override
    public ReservationResponse updateReservation(
            Long id,
            ReservationRequest reservationRequest
    ) {

        // 1. Make sure reservation exists
        logger.info(
                "Updating reservation with id: {}",
                id
        );

        Reservation existingReservation =
                reservationRepository.findById(id)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Cannot update. Reservation not found with id: {}",
                                    id
                            );

                            return new ReservationNotFoundException(id);
                        });


        // 2. Validate date range
        if (!reservationRequest.getCheckOutDate()
                .isAfter(reservationRequest.getCheckInDate())) {

            throw new InvalidReservationDateException(
                    "Check-out date must be after check-in date"
            );
        }


        // 3. Resolve room
        Room room = null;

        if (reservationRequest.getRoomId() != null) {

            room =
                    roomRepository.findById(
                                    reservationRequest.getRoomId()
                            )
                            .orElseThrow(
                                    () -> new RoomNotFoundException(
                                            reservationRequest.getRoomId()
                                    )
                            );
        }


        // 4. Check overlap while ignoring this reservation's own ID
        List<Reservation> overlappingReservations;

        if (room != null) {

            List<ReservationStatus> activeStatuses =
                    List.of(
                            ReservationStatus.PENDING,
                            ReservationStatus.CONFIRMED
                    );

            overlappingReservations =
                    reservationRepository
                            .findByRoom_IdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
                                    room.getId(),
                                    activeStatuses,
                                    reservationRequest.getCheckOutDate(),
                                    reservationRequest.getCheckInDate(),
                                    id
                            );

        } else {

            /*
             * Temporary legacy support for requests
             * that do not yet provide roomId.
             */
            overlappingReservations =
                    reservationRepository
                            .findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThanAndIdNot(
                                    reservationRequest.getRoomType(),
                                    reservationRequest.getCheckOutDate(),
                                    reservationRequest.getCheckInDate(),
                                    id
                            );
        }


        if (overlappingReservations != null
                && !overlappingReservations.isEmpty()) {

            throw new ReservationConflictException(
                    "Reservation overlaps with an existing booking"
            );
        }


        // 5. Update entity
        existingReservation.setCustomerName(
                reservationRequest.getCustomerName()
        );

        existingReservation.setRoom(
                room
        );

        existingReservation.setRoomType(
                reservationRequest.getRoomType()
        );

        existingReservation.setCheckInDate(
                reservationRequest.getCheckInDate()
        );

        existingReservation.setCheckOutDate(
                reservationRequest.getCheckOutDate()
        );


        // 6. Save
        Reservation updatedReservation =
                reservationRepository.save(
                        existingReservation
                );

        logger.info(
                "Reservation updated successfully with id: {}",
                id
        );

        return mapToResponse(
                updatedReservation
        );
    }


    @Override
    public void deleteReservation(
            Long id
    ) {

        logger.info(
                "Deleting reservation with id: {}",
                id
        );

        Reservation existingReservation =
                reservationRepository.findById(id)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Cannot delete. Reservation not found with id: {}",
                                    id
                            );

                            return new ReservationNotFoundException(id);
                        });

        reservationRepository.delete(
                existingReservation
        );

        logger.info(
                "Reservation deleted successfully with id: {}",
                id
        );
    }


    private ReservationResponse mapToResponse(
            Reservation reservation
    ) {

        Long roomId =
                reservation.getRoom() != null
                        ? reservation.getRoom().getId()
                        : null;

        return new ReservationResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getRoomType(),
                reservation.getStatus(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                roomId,
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }

    @Override
    public List<ReservationResponse> searchByCustomerName(
            String customerName
    ) {

        logger.info(
                "Searching reservations for customer: {}",
                customerName
        );

        return reservationRepository
                .findByCustomerNameContainingIgnoreCase(
                        customerName
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public List<ReservationResponse> searchByRoomType(
            String roomType
    ) {

        logger.info(
                "Searching reservations by room type: {}",
                roomType
        );

        List<ReservationResponse> reservations =
                reservationRepository
                        .findByRoomTypeContainingIgnoreCase(
                                roomType
                        )
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        logger.info(
                "Found {} reservations for room type: {}",
                reservations.size(),
                roomType
        );

        return reservations;
    }


    @Override
    public List<ReservationResponse> filterReservations(
            String customerName,
            String roomType
    ) {

        logger.info(
                "Filtering reservations by customerName: {} and roomType: {}",
                customerName,
                roomType
        );

        return reservationRepository
                .findByCustomerNameContainingIgnoreCaseAndRoomTypeContainingIgnoreCase(
                        customerName,
                        roomType
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public Page<ReservationResponse> dynamicFilter(
            String customerName,
            String roomType,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        logger.info(
                "Dynamic filtering - customerName: {}, roomType: {}, page: {}, size: {}, sortBy: {}, direction: {}",
                customerName,
                roomType,
                page,
                size,
                sortBy,
                direction
        );


        Specification<Reservation> specification =
                Specification.where(null);


        if (customerName != null
                && !customerName.isBlank()) {

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder.like(
                                            criteriaBuilder.lower(
                                                    root.get(
                                                            "customerName"
                                                    )
                                            ),
                                            "%"
                                                    + customerName.toLowerCase()
                                                    + "%"
                                    )
                    );
        }


        if (roomType != null
                && !roomType.isBlank()) {

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder.like(
                                            criteriaBuilder.lower(
                                                    root.get(
                                                            "roomType"
                                                    )
                                            ),
                                            "%"
                                                    + roomType.toLowerCase()
                                                    + "%"
                                    )
                    );
        }


        Sort sort =
                direction.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending();


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        Page<Reservation> reservationPage =
                reservationRepository.findAll(
                        specification,
                        pageable
                );


        Page<ReservationResponse> responsePage =
                reservationPage.map(
                        this::mapToResponse
                );


        logger.info(
                "Dynamic filter returned {} reservations",
                responsePage.getNumberOfElements()
        );


        return responsePage;
    }


    @Override
    public ReservationResponse updateReservationStatus(
            Long id,
            ReservationStatus status
    ) {

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(
                                () ->
                                        new ReservationNotFoundException(
                                                id
                                        )
                        );


        ReservationStatus currentStatus =
                reservation.getStatus();


        // Handle old records created before status existed
        if (currentStatus == null) {

            currentStatus =
                    ReservationStatus.PENDING;

            reservation.setStatus(
                    currentStatus
            );
        }


        // CANCELLED is a terminal state
        if (currentStatus
                == ReservationStatus.CANCELLED) {

            throw new InvalidReservationStatusException(
                    "Cancelled reservation status cannot be changed"
            );
        }


        // CONFIRMED cannot return to PENDING
        if (currentStatus
                == ReservationStatus.CONFIRMED
                && status
                == ReservationStatus.PENDING) {

            throw new InvalidReservationStatusException(
                    "Confirmed reservation cannot be changed back to PENDING"
            );
        }


        reservation.setStatus(
                status
        );


        Reservation updatedReservation =
                reservationRepository.save(
                        reservation
                );


        return mapToResponse(
                updatedReservation
        );
    }


    @Override
    public List<ReservationResponse> getReservationsByUsername(
            String username
    ) {

        return reservationRepository
                .findByUser_Username(
                        username
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Override
    public ReservationResponse updateReservation(
            Long id,
            ReservationRequest reservationRequest,
            String username
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found: " + username
                                )
                        );

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(
                                () -> new ReservationNotFoundException(id)
                        );

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(
                        user.getRole()
                );

        boolean isOwner =
                reservation.getUser() != null
                        && reservation.getUser()
                        .getUsername()
                        .equals(username);

        if (!isAdmin && !isOwner) {

            throw new AccessDeniedException(
                    "You are not allowed to update this reservation"
            );
        }

        return updateReservation(
                id,
                reservationRequest
        );
    }
    @Override
    public void deleteReservation(
            Long id,
            String username
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found: " + username
                                )
                        );

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(
                                () -> new ReservationNotFoundException(id)
                        );

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(
                        user.getRole()
                );

        boolean isOwner =
                reservation.getUser() != null
                        && reservation.getUser()
                        .getUsername()
                        .equals(username);

        if (!isAdmin && !isOwner) {

            throw new AccessDeniedException(
                    "You are not allowed to delete this reservation"
            );
        }

        reservationRepository.delete(reservation);
    }
    @Override
    public ReservationResponse cancelReservation(
            Long id,
            String username
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found: " + username
                                )
                        );

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(
                                () -> new ReservationNotFoundException(id)
                        );

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(
                        user.getRole()
                );

        boolean isOwner =
                reservation.getUser() != null
                        && reservation.getUser()
                        .getUsername()
                        .equals(username);

        if (!isAdmin && !isOwner) {

            throw new AccessDeniedException(
                    "You are not allowed to cancel this reservation"
            );
        }

        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new InvalidReservationStatusException(
                    "Reservation is already cancelled"
            );
        }

        reservation.setStatus(
                ReservationStatus.CANCELLED
        );

        Reservation updatedReservation =
                reservationRepository.save(
                        reservation
                );

        return mapToResponse(
                updatedReservation
        );
    }
}