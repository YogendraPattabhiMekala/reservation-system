package com.yogendra.reservation_system.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogendra.reservation_system.dto.ReservationRequest;
import com.yogendra.reservation_system.dto.RoomRequest;
import com.yogendra.reservation_system.entity.User;
import com.yogendra.reservation_system.repository.UserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
class RoomIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    void createRoom_ShouldPersistRoom()
            throws Exception {

        RoomRequest request =
                new RoomRequest(
                        "201",
                        "Deluxe",
                        true
                );

        mockMvc.perform(
                        post("/rooms")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.roomNumber")
                                .value("201")
                )
                .andExpect(
                        jsonPath("$.roomType")
                                .value("Deluxe")
                )
                .andExpect(
                        jsonPath("$.available")
                                .value(true)
                );
    }


    @Test
    void createThenGetRoomById_ShouldReturnPersistedRoom()
            throws Exception {

        RoomRequest request =
                new RoomRequest(
                        "202",
                        "Deluxe",
                        true
                );

        String createResponse =
                mockMvc.perform(
                                post("/rooms")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        request
                                                )
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long roomId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        get(
                                "/rooms/" + roomId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(roomId)
                )
                .andExpect(
                        jsonPath("$.roomNumber")
                                .value("202")
                )
                .andExpect(
                        jsonPath("$.roomType")
                                .value("Deluxe")
                )
                .andExpect(
                        jsonPath("$.available")
                                .value(true)
                );
    }


    @Test
    void getAllRooms_ShouldReturnPersistedRooms()
            throws Exception {

        RoomRequest room1 =
                new RoomRequest(
                        "201",
                        "Deluxe",
                        true
                );

        RoomRequest room2 =
                new RoomRequest(
                        "301",
                        "Suite",
                        true
                );

        mockMvc.perform(
                        post("/rooms")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                room1
                                        )
                                )
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/rooms")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                room2
                                        )
                                )
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/rooms")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].roomNumber")
                                .exists()
                )
                .andExpect(
                        jsonPath("$[1].roomNumber")
                                .exists()
                );
    }


    @Test
    void getRoomsByType_ShouldReturnMatchingRooms()
            throws Exception {

        RoomRequest room1 =
                new RoomRequest(
                        "201",
                        "Deluxe",
                        true
                );

        RoomRequest room2 =
                new RoomRequest(
                        "202",
                        "Deluxe",
                        true
                );

        RoomRequest room3 =
                new RoomRequest(
                        "301",
                        "Suite",
                        true
                );

        mockMvc.perform(
                        post("/rooms")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                room1
                                        )
                                )
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/rooms")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                room2
                                        )
                                )
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/rooms")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                room3
                                        )
                                )
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get(
                                "/rooms/type/Deluxe"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].roomType")
                                .value("Deluxe")
                )
                .andExpect(
                        jsonPath("$[1].roomType")
                                .value("Deluxe")
                );
    }


    @Test
    void updateRoom_ShouldPersistChanges()
            throws Exception {

        RoomRequest createRequest =
                new RoomRequest(
                        "201",
                        "Deluxe",
                        true
                );

        String createResponse =
                mockMvc.perform(
                                post("/rooms")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        createRequest
                                                )
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long roomId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        RoomRequest updateRequest =
                new RoomRequest(
                        "201",
                        "Suite",
                        false
                );

        mockMvc.perform(
                        put(
                                "/rooms/" + roomId
                        )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(roomId)
                )
                .andExpect(
                        jsonPath("$.roomNumber")
                                .value("201")
                )
                .andExpect(
                        jsonPath("$.roomType")
                                .value("Suite")
                )
                .andExpect(
                        jsonPath("$.available")
                                .value(false)
                );

        mockMvc.perform(
                        get(
                                "/rooms/" + roomId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.roomType")
                                .value("Suite")
                )
                .andExpect(
                        jsonPath("$.available")
                                .value(false)
                );
    }


    @Test
    void deleteRoom_ShouldRemoveRoom()
            throws Exception {

        RoomRequest createRequest =
                new RoomRequest(
                        "201",
                        "Deluxe",
                        true
                );

        String createResponse =
                mockMvc.perform(
                                post("/rooms")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        createRequest
                                                )
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long roomId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        delete(
                                "/rooms/" + roomId
                        )
                )
                .andExpect(
                        status().isNoContent()
                );

        mockMvc.perform(
                        get(
                                "/rooms/" + roomId
                        )
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Room not found with id: "
                                                + roomId
                                )
                );
    }


    @Test
    void getAvailableRooms_ShouldReturnOnlyFreeAndAvailableRooms()
            throws Exception {

        RoomRequest room201Request =
                new RoomRequest(
                        "201",
                        "Deluxe",
                        true
                );

        RoomRequest room202Request =
                new RoomRequest(
                        "202",
                        "Deluxe",
                        false
                );

        RoomRequest room203Request =
                new RoomRequest(
                        "203",
                        "Deluxe",
                        true
                );

        String room201Response =
                mockMvc.perform(
                                post("/rooms")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        room201Request
                                                )
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        mockMvc.perform(
                        post("/rooms")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                room202Request
                                        )
                                )
                )
                .andExpect(status().isCreated());

        String room203Response =
                mockMvc.perform(
                                post("/rooms")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        room203Request
                                                )
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long room201Id =
                objectMapper
                        .readTree(room201Response)
                        .get("id")
                        .asLong();

        long room203Id =
                objectMapper
                        .readTree(room203Response)
                        .get("id")
                        .asLong();


        // Create the user that ReservationService
        // will attach to the reservation
        User david =
                new User();

        david.setUsername(
                "davidroomtest"
        );

        david.setEmail(
                "davidroomtest@test.com"
        );

        david.setPassword(
                passwordEncoder.encode(
                        "Password123"
                )
        );

        david.setRole(
                "USER"
        );

        userRepository.save(
                david
        );


        /*
         * Security filters are disabled in this test class.
         *
         * Therefore we provide the Authentication object
         * directly as the request Principal.
         */
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "davidroomtest",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );


        ReservationRequest reservationRequest =
                new ReservationRequest(
                        "David",
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
                        ),
                        room201Id
                );


        mockMvc.perform(
                        post("/reservation")
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                reservationRequest
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                );


        mockMvc.perform(
                        get("/rooms/available")
                                .param(
                                        "roomType",
                                        "Deluxe"
                                )
                                .param(
                                        "checkInDate",
                                        "2026-08-20"
                                )
                                .param(
                                        "checkOutDate",
                                        "2026-08-25"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(room203Id)
                )
                .andExpect(
                        jsonPath("$[0].roomNumber")
                                .value("203")
                )
                .andExpect(
                        jsonPath("$[0].roomType")
                                .value("Deluxe")
                )
                .andExpect(
                        jsonPath("$[0].available")
                                .value(true)
                );
    }


    @Test
    void getAvailableRooms_ShouldReturnBadRequest_WhenDatesAreInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/rooms/available")
                                .param(
                                        "roomType",
                                        "Deluxe"
                                )
                                .param(
                                        "checkInDate",
                                        "2026-08-25"
                                )
                                .param(
                                        "checkOutDate",
                                        "2026-08-20"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Check-out date must be after check-in date"
                                )
                );
    }
}