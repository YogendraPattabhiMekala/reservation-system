package com.yogendra.reservation_system.controller;

import com.yogendra.reservation_system.dto.RoomRequest;
import com.yogendra.reservation_system.dto.RoomResponse;
import com.yogendra.reservation_system.service.RoomService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(
            RoomService roomService
    ) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @Valid @RequestBody RoomRequest roomRequest
    ) {

        RoomResponse createdRoom =
                roomService.createRoom(roomRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdRoom);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {

        return ResponseEntity.ok(
                roomService.getAllRooms()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                roomService.getRoomById(id)
        );
    }

    @GetMapping("/type/{roomType}")
    public ResponseEntity<List<RoomResponse>> getRoomsByType(
            @PathVariable String roomType
    ) {

        return ResponseEntity.ok(
                roomService.getRoomsByType(roomType)
        );
    }
    @GetMapping("/available")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam String roomType,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate
    ) {

        return ResponseEntity.ok(
                roomService.getAvailableRooms(
                        roomType,
                        checkInDate,
                        checkOutDate
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest roomRequest
    ) {

        return ResponseEntity.ok(
                roomService.updateRoom(
                        id,
                        roomRequest
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id
    ) {

        roomService.deleteRoom(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}