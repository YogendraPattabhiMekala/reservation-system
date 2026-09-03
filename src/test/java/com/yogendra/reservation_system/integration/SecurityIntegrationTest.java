package com.yogendra.reservation_system.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogendra.reservation_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import com.yogendra.reservation_system.dto.LoginRequest;
import com.yogendra.reservation_system.entity.User;
import com.yogendra.reservation_system.dto.ReservationRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.yogendra.reservation_system.dto.LoginRequest;
import com.yogendra.reservation_system.dto.ReservationRequest;
import com.yogendra.reservation_system.entity.User;
import com.yogendra.reservation_system.repository.UserRepository;
import com.yogendra.reservation_system.repository.RoomRepository;
import com.yogendra.reservation_system.repository.ReservationRepository;

import com.yogendra.reservation_system.entity.User;
import com.yogendra.reservation_system.model.Room;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.yogendra.reservation_system.dto.RoomRequest;
import java.time.LocalDate;
import com.yogendra.reservation_system.model.Reservation;
import com.yogendra.reservation_system.repository.ReservationRepository;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class SecurityIntegrationTest {
    @Test
    void getReservations_WithoutToken_ShouldBeBlocked()
            throws Exception {

        mockMvc.perform(
                        get("/reservation")
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void userToken_ShouldAllowGetReservations() throws Exception {

        User user = new User();
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setPassword(
                passwordEncoder.encode("Password123")
        );
        user.setRole("USER");

        userRepository.save(user);

        LoginRequest loginRequest =
                new LoginRequest(
                        "john",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        mockMvc.perform(
                        get("/reservation")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }
    @Test
    void userToken_ShouldAllowCreateReservation() throws Exception {

        User user = new User();
        user.setUsername("reservationuser");
        user.setEmail("reservationuser@test.com");
        user.setPassword(
                passwordEncoder.encode("Password123")
        );
        user.setRole("USER");

        userRepository.save(user);

        LoginRequest loginRequest =
                new LoginRequest(
                        "reservationuser",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        loginRequest
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        ReservationRequest reservationRequest =
                new ReservationRequest(
                        "Reservation User",
                        "Deluxe",
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 15)
                );

        mockMvc.perform(
                        post("/reservation")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                reservationRequest
                                        )
                                )
                )
                .andExpect(status().isCreated());
    }
    @Test
    void adminToken_ShouldAllowCreateReservation() throws Exception {

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(
                passwordEncoder.encode("AdminPassword123")
        );
        admin.setRole("ADMIN");

        userRepository.save(admin);

        LoginRequest loginRequest =
                new LoginRequest(
                        "admin",
                        "AdminPassword123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        ReservationRequest reservationRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25)
                );

        mockMvc.perform(
                        post("/reservation")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(reservationRequest)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("David"))
                .andExpect(jsonPath("$.roomType").value("Deluxe"));
    }
    @Test
    void adminToken_ShouldAllowUpdateReservation() throws Exception {

        // 1. Create ADMIN user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(
                passwordEncoder.encode("AdminPassword123")
        );
        admin.setRole("ADMIN");

        userRepository.save(admin);

        // 2. Login as ADMIN
        LoginRequest loginRequest =
                new LoginRequest(
                        "admin",
                        "AdminPassword123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        // 3. Create reservation using ADMIN token
        ReservationRequest createRequest =
                new ReservationRequest(
                        "David",
                        "Suite",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25)
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(createRequest)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long reservationId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        // 4. Update reservation
        ReservationRequest updateRequest =
                new ReservationRequest(
                        "David",
                        "Suite",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25)
                );

        mockMvc.perform(
                        put("/reservation/" + reservationId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(updateRequest)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.customerName").value("David"))
                .andExpect(jsonPath("$.roomType").value("Suite"));
    }
    @Test
    void adminToken_ShouldAllowDeleteReservation() throws Exception {

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(
                passwordEncoder.encode("AdminPassword123")
        );
        admin.setRole("ADMIN");

        userRepository.save(admin);

        LoginRequest loginRequest =
                new LoginRequest(
                        "admin",
                        "AdminPassword123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        ReservationRequest createRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25)
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(createRequest)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long reservationId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        delete("/reservation/" + reservationId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/reservation/" + reservationId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void userToken_ShouldAllowGetRooms() throws Exception {

        User user = new User();
        user.setUsername("roomuser");
        user.setEmail("roomuser@test.com");
        user.setPassword(
                passwordEncoder.encode("Password123")
        );
        user.setRole("USER");

        userRepository.save(user);

        LoginRequest loginRequest =
                new LoginRequest(
                        "roomuser",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        mockMvc.perform(
                        get("/rooms")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }
    @Test
    void userToken_ShouldNotAllowCreateRoom() throws Exception {

        User user = new User();
        user.setUsername("roomuser");
        user.setEmail("roomuser2@test.com");
        user.setPassword(
                passwordEncoder.encode("Password123")
        );
        user.setRole("USER");

        userRepository.save(user);

        LoginRequest loginRequest =
                new LoginRequest(
                        "roomuser",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        RoomRequest roomRequest =
                new RoomRequest(
                        "201",
                        "Deluxe",
                        true
                );

        mockMvc.perform(
                        post("/rooms")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(roomRequest)
                                )
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void adminToken_ShouldAllowCreateRoom() throws Exception {

        User admin = new User();
        admin.setUsername("roomadmin");
        admin.setEmail("roomadmin@test.com");
        admin.setPassword(
                passwordEncoder.encode("Password123")
        );
        admin.setRole("ADMIN");

        userRepository.save(admin);

        LoginRequest loginRequest =
                new LoginRequest(
                        "roomadmin",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        RoomRequest roomRequest =
                new RoomRequest(
                        "501",
                        "Deluxe",
                        true
                );

        mockMvc.perform(
                        post("/rooms")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(roomRequest)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.roomNumber").value("501"))
                .andExpect(jsonPath("$.roomType").value("Deluxe"))
                .andExpect(jsonPath("$.available").value(true));
    }
    @Test
    void userToken_ShouldNotAllowUpdateRoom() throws Exception {

        User user = new User();
        user.setUsername("roomupdateuser");
        user.setEmail("roomupdateuser@test.com");
        user.setPassword(
                passwordEncoder.encode("Password123")
        );
        user.setRole("USER");

        userRepository.save(user);

        LoginRequest loginRequest =
                new LoginRequest(
                        "roomupdateuser",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        RoomRequest roomRequest =
                new RoomRequest(
                        "201",
                        "Suite",
                        false
                );

        mockMvc.perform(
                        put("/rooms/1")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(roomRequest)
                                )
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void userToken_ShouldNotAllowDeleteRoom() throws Exception {

        User user = new User();
        user.setUsername("roomdeleteuser");
        user.setEmail("roomdeleteuser@test.com");
        user.setPassword(
                passwordEncoder.encode("Password123")
        );
        user.setRole("USER");

        userRepository.save(user);

        LoginRequest loginRequest =
                new LoginRequest(
                        "roomdeleteuser",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        mockMvc.perform(
                        delete("/rooms/1")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void adminToken_ShouldAllowUpdateRoom() throws Exception {

        User admin = new User();
        admin.setUsername("roomupdateadmin");
        admin.setEmail("roomupdateadmin@test.com");
        admin.setPassword(
                passwordEncoder.encode("Password123")
        );
        admin.setRole("ADMIN");

        userRepository.save(admin);

        LoginRequest loginRequest =
                new LoginRequest(
                        "roomupdateadmin",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        RoomRequest createRequest =
                new RoomRequest(
                        "601",
                        "Deluxe",
                        true
                );

        String createResponse =
                mockMvc.perform(
                                post("/rooms")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(createRequest)
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
                        "601",
                        "Suite",
                        false
                );

        mockMvc.perform(
                        put("/rooms/" + roomId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(updateRequest)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roomId))
                .andExpect(jsonPath("$.roomType").value("Suite"))
                .andExpect(jsonPath("$.available").value(false));
    }
    @Test
    void adminToken_ShouldAllowDeleteRoom() throws Exception {

        User admin = new User();
        admin.setUsername("roomdeleteadmin");
        admin.setEmail("roomdeleteadmin@test.com");
        admin.setPassword(
                passwordEncoder.encode("Password123")
        );
        admin.setRole("ADMIN");

        userRepository.save(admin);

        LoginRequest loginRequest =
                new LoginRequest(
                        "roomdeleteadmin",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        RoomRequest createRequest =
                new RoomRequest(
                        "701",
                        "Deluxe",
                        true
                );

        String createResponse =
                mockMvc.perform(
                                post("/rooms")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(createRequest)
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
                        delete("/rooms/" + roomId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());
    }
    @Test
    void getMyReservations_ShouldReturnOnlyLoggedInUsersReservations()
            throws Exception {

        User john = new User();
        john.setUsername("johnowner");
        john.setEmail("johnowner@test.com");
        john.setPassword(
                passwordEncoder.encode("Password123")
        );
        john.setRole("USER");

        User david = new User();
        david.setUsername("davidowner");
        david.setEmail("davidowner@test.com");
        david.setPassword(
                passwordEncoder.encode("Password123")
        );
        david.setRole("USER");

        userRepository.save(john);
        userRepository.save(david);

        Room room201 =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        Room room202 =
                new Room(
                        "202",
                        "Deluxe",
                        true
                );

        room201 = roomRepository.save(room201);
        room202 = roomRepository.save(room202);

        // John login
        LoginRequest johnLogin =
                new LoginRequest(
                        "johnowner",
                        "Password123"
                );

        String johnLoginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        johnLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String johnToken =
                objectMapper
                        .readTree(johnLoginResponse)
                        .get("token")
                        .asText();

        // David login
        LoginRequest davidLogin =
                new LoginRequest(
                        "davidowner",
                        "Password123"
                );

        String davidLoginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        davidLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String davidToken =
                objectMapper
                        .readTree(davidLoginResponse)
                        .get("token")
                        .asText();

        // John creates his reservation
        ReservationRequest johnRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        room201.getId()
                );

        mockMvc.perform(
                        post("/reservation")
                                .header(
                                        "Authorization",
                                        "Bearer " + johnToken
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                johnRequest
                                        )
                                )
                )
                .andExpect(status().isCreated());

        // David creates his reservation
        ReservationRequest davidRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        room202.getId()
                );

        mockMvc.perform(
                        post("/reservation")
                                .header(
                                        "Authorization",
                                        "Bearer " + davidToken
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                davidRequest
                                        )
                                )
                )
                .andExpect(status().isCreated());

        // John asks for MY reservations
        mockMvc.perform(
                        get("/reservation/my")
                                .header(
                                        "Authorization",
                                        "Bearer " + johnToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].customerName")
                                .value("John")
                )
                .andExpect(
                        jsonPath("$[0].roomId")
                                .value(room201.getId())
                );
    }
    @Test
    void userToken_ShouldAllowUpdateOwnReservation() throws Exception {

        User john = new User();
        john.setUsername("johnupdateowner");
        john.setEmail("johnupdateowner@test.com");
        john.setPassword(
                passwordEncoder.encode("Password123")
        );
        john.setRole("USER");

        userRepository.save(john);

        Room room201 =
                new Room(
                        "201",
                        "Deluxe",
                        true
                );

        room201 = roomRepository.save(room201);

        LoginRequest loginRequest =
                new LoginRequest(
                        "johnupdateowner",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(loginRequest)
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        ReservationRequest createRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 15),
                        room201.getId()
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
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

        long reservationId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        ReservationRequest updateRequest =
                new ReservationRequest(
                        "John Updated",
                        "Deluxe",
                        LocalDate.of(2026, 9, 16),
                        LocalDate.of(2026, 9, 20),
                        room201.getId()
                );

        mockMvc.perform(
                        put("/reservation/" + reservationId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
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
                        jsonPath("$.customerName")
                                .value("John Updated")
                )
                .andExpect(
                        jsonPath("$.checkInDate")
                                .value("2026-09-16")
                )
                .andExpect(
                        jsonPath("$.checkOutDate")
                                .value("2026-09-20")
                );
    }
    @Test
    void userToken_ShouldNotAllowUpdateAnotherUsersReservation()
            throws Exception {

        User john = new User();
        john.setUsername("johnnotowner");
        john.setEmail("johnnotowner@test.com");
        john.setPassword(
                passwordEncoder.encode("Password123")
        );
        john.setRole("USER");

        User david = new User();
        david.setUsername("davidowner");
        david.setEmail("davidowner2@test.com");
        david.setPassword(
                passwordEncoder.encode("Password123")
        );
        david.setRole("USER");

        userRepository.save(john);
        userRepository.save(david);

        Room room202 =
                new Room(
                        "202",
                        "Deluxe",
                        true
                );

        room202 = roomRepository.save(room202);

        // David login
        LoginRequest davidLogin =
                new LoginRequest(
                        "davidowner",
                        "Password123"
                );

        String davidLoginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        davidLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String davidToken =
                objectMapper
                        .readTree(davidLoginResponse)
                        .get("token")
                        .asText();

        // David creates reservation
        ReservationRequest createRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 10, 10),
                        LocalDate.of(2026, 10, 15),
                        room202.getId()
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .header(
                                                "Authorization",
                                                "Bearer " + davidToken
                                        )
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

        long reservationId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        // John login
        LoginRequest johnLogin =
                new LoginRequest(
                        "johnnotowner",
                        "Password123"
                );

        String johnLoginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        johnLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String johnToken =
                objectMapper
                        .readTree(johnLoginResponse)
                        .get("token")
                        .asText();

        ReservationRequest updateRequest =
                new ReservationRequest(
                        "David Changed",
                        "Deluxe",
                        LocalDate.of(2026, 10, 16),
                        LocalDate.of(2026, 10, 20),
                        room202.getId()
                );

        mockMvc.perform(
                        put("/reservation/" + reservationId)
                                .header(
                                        "Authorization",
                                        "Bearer " + johnToken
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void userToken_ShouldAllowDeleteOwnReservation()
            throws Exception {

        User john = new User();
        john.setUsername("johndeleteowner");
        john.setEmail("johndeleteowner@test.com");
        john.setPassword(
                passwordEncoder.encode("Password123")
        );
        john.setRole("USER");

        userRepository.save(john);

        Room room =
                new Room(
                        "801",
                        "Deluxe",
                        true
                );

        room = roomRepository.save(room);

        LoginRequest loginRequest =
                new LoginRequest(
                        "johndeleteowner",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        loginRequest
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        ReservationRequest createRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 11, 10),
                        LocalDate.of(2026, 11, 15),
                        room.getId()
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
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

        long reservationId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        delete("/reservation/" + reservationId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());
    }
    @Test
    void userToken_ShouldNotAllowDeleteAnotherUsersReservation()
            throws Exception {

        User john = new User();
        john.setUsername("johndeleteother");
        john.setEmail("johndeleteother@test.com");
        john.setPassword(
                passwordEncoder.encode("Password123")
        );
        john.setRole("USER");

        User david = new User();
        david.setUsername("daviddeleteowner");
        david.setEmail("daviddeleteowner@test.com");
        david.setPassword(
                passwordEncoder.encode("Password123")
        );
        david.setRole("USER");

        userRepository.save(john);
        userRepository.save(david);

        Room room =
                new Room(
                        "802",
                        "Deluxe",
                        true
                );

        room = roomRepository.save(room);

        // David login
        LoginRequest davidLogin =
                new LoginRequest(
                        "daviddeleteowner",
                        "Password123"
                );

        String davidLoginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        davidLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String davidToken =
                objectMapper
                        .readTree(davidLoginResponse)
                        .get("token")
                        .asText();

        // David creates his reservation
        ReservationRequest createRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 11, 20),
                        LocalDate.of(2026, 11, 25),
                        room.getId()
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .header(
                                                "Authorization",
                                                "Bearer " + davidToken
                                        )
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

        long reservationId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        // John login
        LoginRequest johnLogin =
                new LoginRequest(
                        "johndeleteother",
                        "Password123"
                );

        String johnLoginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        johnLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String johnToken =
                objectMapper
                        .readTree(johnLoginResponse)
                        .get("token")
                        .asText();

        mockMvc.perform(
                        delete("/reservation/" + reservationId)
                                .header(
                                        "Authorization",
                                        "Bearer " + johnToken
                                )
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void userToken_ShouldAllowCancelOwnReservation()
            throws Exception {

        User john = new User();
        john.setUsername("johncancel");
        john.setEmail("johncancel@test.com");
        john.setPassword(
                passwordEncoder.encode("Password123")
        );
        john.setRole("USER");

        userRepository.save(john);

        Room room =
                new Room(
                        "901",
                        "Deluxe",
                        true
                );

        room = roomRepository.save(room);

        LoginRequest loginRequest =
                new LoginRequest(
                        "johncancel",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        loginRequest
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        ReservationRequest createRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 12, 10),
                        LocalDate.of(2026, 12, 15),
                        room.getId()
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
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

        long reservationId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        patch(
                                "/reservation/"
                                        + reservationId
                                        + "/cancel"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(reservationId)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("CANCELLED")
                );
    }
    @Test
    void userToken_ShouldNotAllowCancelAnotherUsersReservation()
            throws Exception {

        User john = new User();
        john.setUsername("johncancelother");
        john.setEmail("johncancelother@test.com");
        john.setPassword(
                passwordEncoder.encode("Password123")
        );
        john.setRole("USER");

        User david = new User();
        david.setUsername("davidcancelowner");
        david.setEmail("davidcancelowner@test.com");
        david.setPassword(
                passwordEncoder.encode("Password123")
        );
        david.setRole("USER");

        userRepository.save(john);
        userRepository.save(david);

        Room room =
                new Room(
                        "902",
                        "Deluxe",
                        true
                );

        room = roomRepository.save(room);

        // David login
        LoginRequest davidLogin =
                new LoginRequest(
                        "davidcancelowner",
                        "Password123"
                );

        String davidLoginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        davidLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String davidToken =
                objectMapper
                        .readTree(davidLoginResponse)
                        .get("token")
                        .asText();

        // David creates reservation
        ReservationRequest createRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 12, 20),
                        LocalDate.of(2026, 12, 25),
                        room.getId()
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .header(
                                                "Authorization",
                                                "Bearer " + davidToken
                                        )
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

        long reservationId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        // John login
        LoginRequest johnLogin =
                new LoginRequest(
                        "johncancelother",
                        "Password123"
                );

        String johnLoginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        johnLogin
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String johnToken =
                objectMapper
                        .readTree(johnLoginResponse)
                        .get("token")
                        .asText();

        // John tries to cancel David's reservation
        mockMvc.perform(
                        patch(
                                "/reservation/"
                                        + reservationId
                                        + "/cancel"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + johnToken
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.status")
                                .value(403)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Forbidden")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "You are not allowed to cancel this reservation"
                                )
                );
    }
    @Test
    void cancelReservation_ShouldReturnBadRequest_WhenAlreadyCancelled()
            throws Exception {

        User john = new User();
        john.setUsername("johncancelagain");
        john.setEmail("johncancelagain@test.com");
        john.setPassword(
                passwordEncoder.encode("Password123")
        );
        john.setRole("USER");

        userRepository.save(john);

        Room room =
                new Room(
                        "903",
                        "Deluxe",
                        true
                );

        room = roomRepository.save(room);

        LoginRequest loginRequest =
                new LoginRequest(
                        "johncancelagain",
                        "Password123"
                );

        String loginResponse =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        loginRequest
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(loginResponse)
                        .get("token")
                        .asText();

        ReservationRequest createRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 12, 26),
                        LocalDate.of(2026, 12, 30),
                        room.getId()
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
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

        long reservationId =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        // First cancellation succeeds
        mockMvc.perform(
                        patch(
                                "/reservation/"
                                        + reservationId
                                        + "/cancel"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("CANCELLED")
                );

        // Second cancellation should fail
        mockMvc.perform(
                        patch(
                                "/reservation/"
                                        + reservationId
                                        + "/cancel"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
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
                                        "Reservation is already cancelled"
                                )
                );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ReservationRepository reservationRepository;
}