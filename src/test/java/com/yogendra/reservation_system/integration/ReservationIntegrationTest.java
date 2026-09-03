package com.yogendra.reservation_system.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogendra.reservation_system.dto.ReservationRequest;
import com.yogendra.reservation_system.entity.User;
import com.yogendra.reservation_system.model.Room;
import com.yogendra.reservation_system.repository.RoomRepository;
import com.yogendra.reservation_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ReservationIntegrationTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUpAuthenticatedUser() {

        /*
         * MockMvc requests authenticate as "integrationadmin" using the
         * Spring Security request post-processor. This database user lets
         * ReservationServiceImpl resolve the same username and attach ownership.
         */
        if (userRepository
                .findByUsername("integrationadmin")
                .isEmpty()) {

            User user = new User();

            user.setUsername(
                    "integrationadmin"
            );

            user.setEmail(
                    "integrationadmin@test.com"
            );

            /*
             * This password is not used for authentication in this
             * integration class because @WithMockUser supplies the
             * authenticated SecurityContext.
             */
            user.setPassword(
                    "integration-test-password"
            );

            user.setRole(
                    "ADMIN"
            );

            userRepository.save(
                    user
            );
        }
    }


    // Reusable helper for every VALID reservation request
    private ReservationRequest validRequest(
            String customerName,
            String roomType
    ) {

        return new ReservationRequest(
                customerName,
                roomType,
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
    }


    private ReservationRequest validRequest(
            String customerName,
            String roomType,
            int checkInDay,
            int checkOutDay
    ) {

        return new ReservationRequest(
                customerName,
                roomType,
                LocalDate.of(
                        2026,
                        8,
                        checkInDay
                ),
                LocalDate.of(
                        2026,
                        8,
                        checkOutDay
                )
        );
    }


    @Test
    void createReservation_ShouldPersistReservation()
            throws Exception {

        ReservationRequest request =
                validRequest(
                        "David",
                        "Deluxe"
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("David"))
                .andExpect(jsonPath("$.roomType").value("Deluxe"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.checkInDate").value("2026-08-20"))
                .andExpect(jsonPath("$.checkOutDate").value("2026-08-25"));
    }

    @Test
    void createThenGetReservation_ShouldReturnCreatedReservation()
            throws Exception {

        ReservationRequest request =
                validRequest(
                        "David",
                        "Deluxe"
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation")
                                        .with(user("integrationadmin").roles("ADMIN"))
                                        .contentType("application/json")
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long id =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        get("/reservation/" + id)
                                .with(user("integrationadmin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.customerName").value("David"))
                .andExpect(jsonPath("$.roomType").value("Deluxe"))
                .andExpect(jsonPath("$.checkInDate").value("2026-08-20"))
                .andExpect(jsonPath("$.checkOutDate").value("2026-08-25"));
    }


    @Test
    void createThenUpdateReservation_ShouldPersistChanges()
            throws Exception {

        ReservationRequest createRequest =
                validRequest(
                        "David",
                        "Deluxe"
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
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

        long id =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        ReservationRequest updateRequest =
                new ReservationRequest(
                        "David Updated",
                        "Suite",
                        LocalDate.of(2026, 8, 21),
                        LocalDate.of(2026, 8, 27)
                );

        mockMvc.perform(
                        put("/reservation/" + id).with(user("integrationadmin").roles("ADMIN"))
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
                                .value("David Updated")
                )
                .andExpect(
                        jsonPath("$.roomType")
                                .value("Suite")
                )
                .andExpect(
                        jsonPath("$.checkInDate")
                                .value("2026-08-21")
                )
                .andExpect(
                        jsonPath("$.checkOutDate")
                                .value("2026-08-27")
                );
    }


    @Test
    void createThenDeleteReservation_ShouldReturnNotFoundWhenFetched()
            throws Exception {

        ReservationRequest request =
                validRequest(
                        "Sam",
                        "Standard"
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long id =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        delete("/reservation/" + id).with(user("integrationadmin").roles("ADMIN"))
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/reservation/" + id).with(user("integrationadmin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }


    @Test
    void searchByCustomerName_ShouldReturnMatchingReservations()
            throws Exception {

        ReservationRequest r1 =
                validRequest(
                        "David",
                        "Deluxe"
                );

        ReservationRequest r2 =
                validRequest(
                        "Daniel",
                        "Suite"
                );

        ReservationRequest r3 =
                validRequest(
                        "Roy",
                        "Standard"
                );

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r1)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r2)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r3)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/reservation/search").with(user("integrationadmin").roles("ADMIN"))
                                .param(
                                        "customerName",
                                        "da"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    @Test
    void filterByRoomType_ShouldReturnMatchingReservations()
            throws Exception {

        ReservationRequest r1 =
                validRequest(
                        "David",
                        "Deluxe",
                        10,
                        13
                );

        ReservationRequest r2 =
                validRequest(
                        "Roy",
                        "Deluxe",
                        14,
                        17
                );

        ReservationRequest r3 =
                validRequest(
                        "Mike",
                        "Standard",
                        10,
                        13
                );

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r1)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r2)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r3)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/reservation/filter").with(user("integrationadmin").roles("ADMIN"))
                                .param(
                                        "roomType",
                                        "del"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
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
    void advancedFilter_ShouldReturnMatchingReservations()
            throws Exception {

        ReservationRequest r1 =
                validRequest(
                        "David",
                        "Deluxe",
                        10,
                        13
                );

        ReservationRequest r2 =
                validRequest(
                        "David",
                        "Suite",
                        10,
                        13
                );

        ReservationRequest r3 =
                validRequest(
                        "Roy",
                        "Deluxe",
                        14,
                        17
                );

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r1)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r2)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r3)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/reservation/advanced-filter").with(user("integrationadmin").roles("ADMIN"))
                                .param("customerName", "dav")
                                .param("roomType", "del")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].customerName")
                                .value("David")
                )
                .andExpect(
                        jsonPath("$[0].roomType")
                                .value("Deluxe")
                );
    }


    @Test
    void dynamicFilter_ShouldReturnFilteredPagedAndSortedReservations()
            throws Exception {

        ReservationRequest r1 =
                validRequest(
                        "David",
                        "Deluxe",
                        10,
                        13
                );

        ReservationRequest r2 =
                validRequest(
                        "Daniel",
                        "Deluxe",
                        14,
                        17
                );

        ReservationRequest r3 =
                validRequest(
                        "David",
                        "Suite",
                        10,
                        13
                );

        ReservationRequest r4 =
                validRequest(
                        "Roy",
                        "Deluxe",
                        18,
                        21
                );

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r1)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r2)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r3)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(r4)
                        )
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/reservation/dynamic-filter").with(user("integrationadmin").roles("ADMIN"))
                                .param("customerName", "da")
                                .param("roomType", "del")
                                .param("page", "0")
                                .param("size", "2")
                                .param("sortBy", "customerName")
                                .param("direction", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.content[0].customerName")
                                .value("Daniel")
                )
                .andExpect(
                        jsonPath("$.content[1].customerName")
                                .value("David")
                );
    }


    @Test
    void updateReservationStatus_ShouldPersistStatusChange()
            throws Exception {

        ReservationRequest request =
                validRequest(
                        "David",
                        "Deluxe"
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long id =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        patch(
                                "/reservation/"
                                        + id
                                        + "/status"
                        ).with(user("integrationadmin").roles("ADMIN"))
                                .param(
                                        "status",
                                        "CONFIRMED"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("CONFIRMED")
                );

        mockMvc.perform(
                        get("/reservation/" + id).with(user("integrationadmin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("CONFIRMED")
                );
    }


    @Test
    void cancelledReservation_ShouldNotBeChangedToConfirmed()
            throws Exception {

        ReservationRequest request =
                validRequest(
                        "John",
                        "Deluxe"
                );

        String createResponse =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long id =
                objectMapper
                        .readTree(createResponse)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        patch(
                                "/reservation/"
                                        + id
                                        + "/status"
                        ).with(user("integrationadmin").roles("ADMIN"))
                                .param(
                                        "status",
                                        "CONFIRMED"
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch(
                                "/reservation/"
                                        + id
                                        + "/status"
                        ).with(user("integrationadmin").roles("ADMIN"))
                                .param(
                                        "status",
                                        "CANCELLED"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("CANCELLED")
                );

        mockMvc.perform(
                        patch(
                                "/reservation/"
                                        + id
                                        + "/status"
                        ).with(user("integrationadmin").roles("ADMIN"))
                                .param(
                                        "status",
                                        "CONFIRMED"
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
                                        "Cancelled reservation status cannot be changed"
                                )
                );
    }


    @Test
    void createReservation_ShouldPersistCheckInAndCheckOutDates()
            throws Exception {

        ReservationRequest request =
                validRequest(
                        "David",
                        "Deluxe"
                );

        String responseBody =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                        .contentType("application/json")
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andExpect(
                                jsonPath("$.checkInDate")
                                        .value("2026-08-20")
                        )
                        .andExpect(
                                jsonPath("$.checkOutDate")
                                        .value("2026-08-25")
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long id =
                objectMapper
                        .readTree(responseBody)
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        get("/reservation/" + id).with(user("integrationadmin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.checkInDate")
                                .value("2026-08-20")
                )
                .andExpect(
                        jsonPath("$.checkOutDate")
                                .value("2026-08-25")
                );
    }
    @Test
    void createReservation_ShouldReturnBadRequest_WhenCheckOutDateIsBeforeCheckInDate()
            throws Exception {

        ReservationRequest request =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 15)
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
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
                                        "Check-out date must be after check-in date"
                                )
                );
    }
    @Test
    void createReservation_ShouldReturnConflict_WhenReservationOverlaps()
            throws Exception {

        // First reservation
        ReservationRequest firstRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25)
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(firstRequest)
                                )
                )
                .andExpect(status().isCreated());

        // Second reservation overlaps with first reservation
        ReservationRequest overlappingRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 23),
                        LocalDate.of(2026, 8, 27)
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(overlappingRequest)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Reservation overlaps with an existing booking"
                                )
                );
    }
    @Test
    void updateReservation_ShouldReturnConflict_WhenUpdatedDatesOverlapAnotherReservation()
            throws Exception {

        // Reservation A
        ReservationRequest firstRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 15)
                );

        String firstResponse =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(firstRequest)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long firstId =
                objectMapper
                        .readTree(firstResponse)
                        .get("id")
                        .asLong();


        // Reservation B - initially non-overlapping
        ReservationRequest secondRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25)
                );

        String secondResponse =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(secondRequest)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long secondId =
                objectMapper
                        .readTree(secondResponse)
                        .get("id")
                        .asLong();


        // Now update Reservation B so that it overlaps Reservation A
        ReservationRequest conflictingUpdate =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 12),
                        LocalDate.of(2026, 8, 18)
                );

        mockMvc.perform(
                        put("/reservation/" + secondId).with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(conflictingUpdate)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Reservation overlaps with an existing booking"
                                )
                );
    }
    @Test
    void createReservations_ShouldAllowSameDates_WhenRoomsAreDifferent()
            throws Exception {

        Room room201 = new Room(
                "201",
                "Deluxe",
                true
        );

        Room room202 = new Room(
                "202",
                "Deluxe",
                true
        );

        room201 = roomRepository.save(room201);
        room202 = roomRepository.save(room202);

        ReservationRequest firstRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        room201.getId()
                );

        ReservationRequest secondRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        room202.getId()
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(firstRequest)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.roomId")
                                .value(room201.getId())
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(secondRequest)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.roomId")
                                .value(room202.getId())
                );
    }
    @Test
    void createReservations_ShouldReturnConflict_WhenSameRoomOverlaps()
            throws Exception {

        Room room201 = new Room(
                "201",
                "Deluxe",
                true
        );

        room201 = roomRepository.save(room201);

        ReservationRequest firstRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        room201.getId()
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(firstRequest)
                                )
                )
                .andExpect(status().isCreated());

        ReservationRequest overlappingRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 22),
                        LocalDate.of(2026, 8, 27),
                        room201.getId()
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(overlappingRequest)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Reservation overlaps with an existing booking"
                                )
                );
    }
    @Test
    void updateReservation_ShouldReturnConflict_WhenSameRoomOverlapsAnotherReservation()
            throws Exception {

        Room room201 = new Room(
                "201",
                "Deluxe",
                true
        );

        room201 = roomRepository.save(room201);

        // Reservation A
        ReservationRequest firstRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 15),
                        room201.getId()
                );

        String firstResponse =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(firstRequest)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long firstId =
                objectMapper
                        .readTree(firstResponse)
                        .get("id")
                        .asLong();

        // Reservation B - initially non-overlapping
        ReservationRequest secondRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        room201.getId()
                );

        String secondResponse =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(secondRequest)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long secondId =
                objectMapper
                        .readTree(secondResponse)
                        .get("id")
                        .asLong();

        // Update Reservation B so it overlaps Reservation A
        ReservationRequest conflictingUpdate =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 12),
                        LocalDate.of(2026, 8, 18),
                        room201.getId()
                );

        mockMvc.perform(
                        put("/reservation/" + secondId).with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(conflictingUpdate)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Reservation overlaps with an existing booking"
                                )
                );
    }
    @Test
    void updateReservation_ShouldAllowSameDates_WhenRoomIsDifferent()
            throws Exception {

        Room room201 = new Room(
                "201",
                "Deluxe",
                true
        );

        Room room202 = new Room(
                "202",
                "Deluxe",
                true
        );

        room201 = roomRepository.save(room201);
        room202 = roomRepository.save(room202);

        // David already has Room 201
        ReservationRequest firstRequest =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        room201.getId()
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(firstRequest)
                                )
                )
                .andExpect(status().isCreated());

        // John initially has Room 202 on different dates
        ReservationRequest secondRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 15),
                        room202.getId()
                );

        String secondResponse =
                mockMvc.perform(
                                post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(secondRequest)
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        long secondId =
                objectMapper
                        .readTree(secondResponse)
                        .get("id")
                        .asLong();

        // Update John to the SAME dates as David,
        // but keep him in Room 202
        ReservationRequest updateRequest =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25),
                        room202.getId()
                );

        mockMvc.perform(
                        put("/reservation/" + secondId).with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(updateRequest)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("John"))
                .andExpect(jsonPath("$.roomId").value(room202.getId()))
                .andExpect(jsonPath("$.checkInDate").value("2026-08-20"))
                .andExpect(jsonPath("$.checkOutDate").value("2026-08-25"));
    }
    @Test
    void createReservation_ShouldReturnCreatedAtAndUpdatedAt()
            throws Exception {

        ReservationRequest request =
                validRequest(
                        "David",
                        "Deluxe"
                );

        mockMvc.perform(
                        post("/reservation").with(user("integrationadmin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.createdAt").exists()
                )
                .andExpect(
                        jsonPath("$.updatedAt").exists()
                )
                .andExpect(
                        jsonPath("$.createdAt").isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.updatedAt").isNotEmpty()
                );
    }
}