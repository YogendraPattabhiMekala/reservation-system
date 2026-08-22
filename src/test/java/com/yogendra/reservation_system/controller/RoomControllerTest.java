package com.yogendra.reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogendra.reservation_system.dto.RoomRequest;
import com.yogendra.reservation_system.dto.RoomResponse;
import com.yogendra.reservation_system.service.RoomService;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.junit.jupiter.api.Test;
import com.yogendra.reservation_system.exception.RoomNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import com.yogendra.reservation_system.exception.InvalidReservationDateException;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.yogendra.reservation_system.service.JwtService;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import java.util.List;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {
    @Test
    void createRoom_ShouldReturnCreatedRoom()
            throws Exception {

        RoomRequest request =
                new RoomRequest(
                        "201",
                        "Deluxe",
                        true
                );

        RoomResponse response =
                new RoomResponse(
                        1L,
                        "201",
                        "Deluxe",
                        true
                );

        when(roomService.createRoom(any(RoomRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/rooms")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomNumber").value("201"))
                .andExpect(jsonPath("$.roomType").value("Deluxe"))
                .andExpect(jsonPath("$.available").value(true));
    }
    @Test
    void getAllRooms_ShouldReturnAllRooms()
            throws Exception {

        RoomResponse room1 =
                new RoomResponse(
                        1L,
                        "201",
                        "Deluxe",
                        true
                );

        RoomResponse room2 =
                new RoomResponse(
                        2L,
                        "301",
                        "Suite",
                        true
                );

        when(roomService.getAllRooms())
                .thenReturn(
                        List.of(
                                room1,
                                room2
                        )
                );

        mockMvc.perform(
                        get("/rooms")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].roomNumber").value("201"))
                .andExpect(jsonPath("$[0].roomType").value("Deluxe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].roomNumber").value("301"))
                .andExpect(jsonPath("$[1].roomType").value("Suite"));
    }
    @Test
    void getRoomById_ShouldReturnRoom()
            throws Exception {

        RoomResponse response =
                new RoomResponse(
                        1L,
                        "201",
                        "Deluxe",
                        true
                );

        when(roomService.getRoomById(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/rooms/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomNumber").value("201"))
                .andExpect(jsonPath("$.roomType").value("Deluxe"))
                .andExpect(jsonPath("$.available").value(true));
    }
    @Test
    void getRoomById_ShouldReturnNotFound_WhenRoomDoesNotExist()
            throws Exception {

        when(roomService.getRoomById(999L))
                .thenThrow(
                        new RoomNotFoundException(999L)
                );

        mockMvc.perform(
                        get("/rooms/999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Room not found with id: 999")
                );
    }
    @Test
    void getRoomsByType_ShouldReturnMatchingRooms()
            throws Exception {

        RoomResponse room1 =
                new RoomResponse(
                        1L,
                        "201",
                        "Deluxe",
                        true
                );

        RoomResponse room2 =
                new RoomResponse(
                        2L,
                        "202",
                        "Deluxe",
                        true
                );

        when(roomService.getRoomsByType("Deluxe"))
                .thenReturn(
                        List.of(
                                room1,
                                room2
                        )
                );

        mockMvc.perform(
                        get("/rooms/type/Deluxe")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomNumber").value("201"))
                .andExpect(jsonPath("$[0].roomType").value("Deluxe"))
                .andExpect(jsonPath("$[1].roomNumber").value("202"))
                .andExpect(jsonPath("$[1].roomType").value("Deluxe"));
    }
    @Test
    void updateRoom_ShouldReturnUpdatedRoom()
            throws Exception {

        RoomRequest request =
                new RoomRequest(
                        "201",
                        "Suite",
                        false
                );

        RoomResponse response =
                new RoomResponse(
                        1L,
                        "201",
                        "Suite",
                        false
                );

        when(
                roomService.updateRoom(
                        eq(1L),
                        any(RoomRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        put("/rooms/1")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomNumber").value("201"))
                .andExpect(jsonPath("$.roomType").value("Suite"))
                .andExpect(jsonPath("$.available").value(false));
    }
    @Test
    void updateRoom_ShouldReturnNotFound_WhenRoomDoesNotExist()
            throws Exception {

        RoomRequest request =
                new RoomRequest(
                        "999",
                        "Suite",
                        true
                );

        when(
                roomService.updateRoom(
                        eq(999L),
                        any(RoomRequest.class)
                )
        ).thenThrow(
                new RoomNotFoundException(999L)
        );

        mockMvc.perform(
                        put("/rooms/999")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Room not found with id: 999")
                );
    }
    @Test
    void deleteRoom_ShouldReturnNoContent()
            throws Exception {

        mockMvc.perform(
                        delete("/rooms/1")
                )
                .andExpect(status().isNoContent());

        verify(roomService).deleteRoom(1L);
    }
    @Test
    void deleteRoom_ShouldReturnNotFound_WhenRoomDoesNotExist()
            throws Exception {

        doThrow(
                new RoomNotFoundException(999L)
        )
                .when(roomService)
                .deleteRoom(999L);

        mockMvc.perform(
                        delete("/rooms/999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Room not found with id: 999")
                );
    }
    @Test
    void getAvailableRooms_ShouldReturnAvailableRooms()
            throws Exception {

        RoomResponse room1 =
                new RoomResponse(
                        2L,
                        "202",
                        "Deluxe",
                        true
                );

        RoomResponse room2 =
                new RoomResponse(
                        3L,
                        "203",
                        "Deluxe",
                        true
                );

        when(
                roomService.getAvailableRooms(
                        eq("Deluxe"),
                        eq(LocalDate.of(2026, 8, 20)),
                        eq(LocalDate.of(2026, 8, 25))
                )
        ).thenReturn(
                List.of(
                        room1,
                        room2
                )
        );

        mockMvc.perform(
                        get("/rooms/available")
                                .param("roomType", "Deluxe")
                                .param("checkInDate", "2026-08-20")
                                .param("checkOutDate", "2026-08-25")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomNumber").value("202"))
                .andExpect(jsonPath("$[1].roomNumber").value("203"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].available").value(true));
    }
    @Test
    void getAvailableRooms_ShouldReturnBadRequest_WhenDatesAreInvalid()
            throws Exception {

        when(
                roomService.getAvailableRooms(
                        eq("Deluxe"),
                        eq(LocalDate.of(2026, 8, 25)),
                        eq(LocalDate.of(2026, 8, 20))
                )
        ).thenThrow(
                new InvalidReservationDateException(
                        "Check-out date must be after check-in date"
                )
        );

        mockMvc.perform(
                        get("/rooms/available")
                                .param("roomType", "Deluxe")
                                .param("checkInDate", "2026-08-25")
                                .param("checkOutDate", "2026-08-20")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Check-out date must be after check-in date"
                                )
                );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomService roomService;
    @MockitoBean
    private JwtService jwtService;
}