package com.yogendra.reservation_system.controller;

import com.yogendra.reservation_system.dto.ReservationRequest;
import com.yogendra.reservation_system.dto.ReservationResponse;
import com.yogendra.reservation_system.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.yogendra.reservation_system.model.ReservationStatus;
import java.util.List;
import org.springframework.security.core.Authentication;
import com.yogendra.reservation_system.model.ReservationStatus;
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }
    @Operation(
            summary = "Get All Reservations",
            description = "Returns all reservations with pagination and sorting."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully")
    })
    @GetMapping
    public Page<ReservationResponse> getAllReservations(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "5")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction
    ) {

        return reservationService.getAllReservations(
                page,
                size,
                sortBy,
                direction
        );
    }
    @Operation(
            summary = "Get Reservation By ID",
            description = "Returns a reservation using its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse>
    getReservationById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                reservationService.getReservationById(id)
        );
    }
    @Operation(
            summary = "Search Reservations",
            description = "Search reservations by customer name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponse>> searchReservations(
            @RequestParam String customerName) {

        return ResponseEntity.ok(
                reservationService.searchByCustomerName(customerName)
        );
    }
    @Operation(
            summary = "Filter Reservations By Room Type",
            description = "Returns reservations filtered by room type."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<ReservationResponse>>
    filterByRoomType(
            @RequestParam String roomType
    ) {

        return ResponseEntity.ok(
                reservationService.searchByRoomType(roomType)
        );
    }
    @Operation(
            summary = "Advanced Reservation Filter",
            description = "Advanced filtering using multiple search parameters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @GetMapping("/advanced-filter")
    public ResponseEntity<List<ReservationResponse>>
    filterReservations(
            @RequestParam String customerName,
            @RequestParam String roomType
    ) {

        return ResponseEntity.ok(
                reservationService.filterReservations(
                        customerName,
                        roomType
                )
        );
    }
    @Operation(
            summary = "Dynamic Reservation Filter",
            description = "Filters reservations using optional criteria with pagination and sorting."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @GetMapping("/dynamic-filter")
    public ResponseEntity<Page<ReservationResponse>> dynamicFilter(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String roomType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {

        return ResponseEntity.ok(
                reservationService.dynamicFilter(
                        customerName,
                        roomType,
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }
    @Operation(
            summary = "Create Reservation",
            description = "Creates a new reservation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest reservationRequest,
            Authentication authentication
    ) {

        ReservationResponse createdReservation =
                reservationService.createReservation(
                        reservationRequest,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReservation);
    }
    @Operation(
            summary = "Update Reservation",
            description = "Updates an existing reservation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation updated"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest reservationRequest,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                reservationService.updateReservation(
                        id,
                        reservationRequest,
                        authentication.getName()
                )
        );
    }
    @Operation(
            summary = "Delete Reservation",
            description = "Deletes a reservation by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation deleted"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id,
            Authentication authentication
    ) {

        reservationService.deleteReservation(
                id,
                authentication.getName()
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status
    ) {

        return ResponseEntity.ok(
                reservationService.updateReservationStatus(id, status)
        );
    }
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            Authentication authentication
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                reservationService.getReservationsByUsername(
                        username
                )
        );
    }
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                reservationService.cancelReservation(
                        id,
                        authentication.getName()
                )
        );
    }

}