package com.yogendra.reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogendra.reservation_system.dto.ReservationRequest;
import com.yogendra.reservation_system.dto.ReservationResponse;
import com.yogendra.reservation_system.exception.InvalidReservationStatusException;
import com.yogendra.reservation_system.exception.ReservationConflictException;
import com.yogendra.reservation_system.exception.ReservationNotFoundException;
import com.yogendra.reservation_system.model.ReservationStatus;
import com.yogendra.reservation_system.service.JwtService;
import com.yogendra.reservation_system.service.ReservationService;
import static org.mockito.ArgumentMatchers.argThat;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReservationControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtService jwtService;


    @Test
    void getReservationById_ShouldReturnReservation()
            throws Exception {

        ReservationResponse response =
                new ReservationResponse(
                        1L,
                        "David",
                        "Deluxe"
                );

        when(
                reservationService.getReservationById(1L)
        ).thenReturn(response);

        mockMvc.perform(
                        get("/reservation/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("David"))
                .andExpect(jsonPath("$.roomType").value("Deluxe"));
    }


    @Test
    void getReservationById_ShouldReturnNotFound_WhenReservationDoesNotExist()
            throws Exception {

        when(
                reservationService.getReservationById(99L)
        ).thenThrow(
                new ReservationNotFoundException(99L)
        );

        mockMvc.perform(
                        get("/reservation/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Reservation not found with id: 99"
                                )
                );
    }


    @Test
    void createReservation_ShouldReturnCreatedReservation()
            throws Exception {

        ReservationRequest request =
                new ReservationRequest(
                        "Roy",
                        "Deluxe"
                );

        request.setCheckInDate(
                LocalDate.of(2026, 8, 20)
        );

        request.setCheckOutDate(
                LocalDate.of(2026, 8, 25)
        );

        ReservationResponse response =
                new ReservationResponse(
                        5L,
                        "Roy",
                        "Deluxe"
                );

        when(
                reservationService.createReservation(
                        any(ReservationRequest.class),
                        eq("john")
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/reservation")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.customerName").value("Roy"))
                .andExpect(jsonPath("$.roomType").value("Deluxe"));
    }


    @Test
    void updateReservation_ShouldReturnUpdatedReservation()
            throws Exception {

        ReservationRequest request =
                new ReservationRequest(
                        "Roy",
                        "Suite"
                );

        request.setCheckInDate(
                LocalDate.of(2026, 8, 20)
        );

        request.setCheckOutDate(
                LocalDate.of(2026, 8, 25)
        );

        ReservationResponse response =
                new ReservationResponse(
                        5L,
                        "Roy",
                        "Suite"
                );

        when(
                reservationService.updateReservation(
                        eq(5L),
                        any(ReservationRequest.class),
                        eq("john")
                )
        ).thenReturn(response);

        mockMvc.perform(
                        put("/reservation/5")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.customerName").value("Roy"))
                .andExpect(jsonPath("$.roomType").value("Suite"));
    }


    @Test
    void deleteReservation_ShouldReturnNoContent() throws Exception {

        doNothing()
                .when(reservationService)
                .deleteReservation(
                        5L,
                        "john"
                );

        mockMvc.perform(
                        delete("/reservation/5")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
                                )
                )
                .andExpect(status().isNoContent());

        verify(reservationService)
                .deleteReservation(
                        5L,
                        "john"
                );
    }


    @Test
    void getAllReservations_ShouldReturnPagedReservations()
            throws Exception {

        ReservationResponse r1 =
                new ReservationResponse(
                        1L,
                        "David",
                        "Deluxe"
                );

        ReservationResponse r2 =
                new ReservationResponse(
                        2L,
                        "Roy",
                        "Suite"
                );

        Page<ReservationResponse> page =
                new PageImpl<>(
                        List.of(
                                r1,
                                r2
                        )
                );

        when(
                reservationService.getAllReservations(
                        0,
                        2,
                        "id",
                        "asc"
                )
        ).thenReturn(page);

        mockMvc.perform(
                        get("/reservation")
                                .param("page", "0")
                                .param("size", "2")
                                .param("sortBy", "id")
                                .param("direction", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(
                        jsonPath("$.content[0].customerName")
                                .value("David")
                )
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(
                        jsonPath("$.content[1].customerName")
                                .value("Roy")
                );
    }


    @Test
    void searchReservations_ShouldReturnMatchingReservations()
            throws Exception {

        ReservationResponse response =
                new ReservationResponse(
                        1L,
                        "David",
                        "Deluxe"
                );

        List<ReservationResponse> reservations =
                List.of(response);

        when(
                reservationService.searchByCustomerName("David")
        ).thenReturn(reservations);

        mockMvc.perform(
                        get("/reservation/search")
                                .param(
                                        "customerName",
                                        "David"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
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
    void filterByRoomType_ShouldReturnReservations()
            throws Exception {

        ReservationResponse response =
                new ReservationResponse(
                        2L,
                        "Roy",
                        "Suite"
                );

        List<ReservationResponse> reservations =
                List.of(response);

        when(
                reservationService.searchByRoomType("Suite")
        ).thenReturn(reservations);

        mockMvc.perform(
                        get("/reservation/filter")
                                .param(
                                        "roomType",
                                        "Suite"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(
                        jsonPath("$[0].customerName")
                                .value("Roy")
                )
                .andExpect(
                        jsonPath("$[0].roomType")
                                .value("Suite")
                );
    }


    @Test
    void advancedFilter_ShouldReturnMatchingReservations()
            throws Exception {

        ReservationResponse response =
                new ReservationResponse(
                        3L,
                        "Roy",
                        "Suite"
                );

        List<ReservationResponse> reservations =
                List.of(response);

        when(
                reservationService.filterReservations(
                        "Roy",
                        "Suite"
                )
        ).thenReturn(reservations);

        mockMvc.perform(
                        get("/reservation/advanced-filter")
                                .param(
                                        "customerName",
                                        "Roy"
                                )
                                .param(
                                        "roomType",
                                        "Suite"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(
                        jsonPath("$[0].customerName")
                                .value("Roy")
                )
                .andExpect(
                        jsonPath("$[0].roomType")
                                .value("Suite")
                );
    }


    @Test
    void dynamicFilter_ShouldReturnPagedReservations()
            throws Exception {

        ReservationResponse response =
                new ReservationResponse(
                        4L,
                        "John",
                        "Deluxe"
                );

        Page<ReservationResponse> page =
                new PageImpl<>(
                        List.of(response)
                );

        when(
                reservationService.dynamicFilter(
                        "John",
                        "Deluxe",
                        0,
                        5,
                        "id",
                        "asc"
                )
        ).thenReturn(page);

        mockMvc.perform(
                        get("/reservation/dynamic-filter")
                                .param("customerName", "John")
                                .param("roomType", "Deluxe")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sortBy", "id")
                                .param("direction", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(4)
                )
                .andExpect(
                        jsonPath("$.content[0].customerName")
                                .value("John")
                )
                .andExpect(
                        jsonPath("$.content[0].roomType")
                                .value("Deluxe")
                );
    }


    @Test
    void updateReservationStatus_ShouldReturnUpdatedReservation()
            throws Exception {

        ReservationResponse response =
                new ReservationResponse(
                        8L,
                        "john",
                        "Deluxe",
                        ReservationStatus.CONFIRMED
                );

        when(
                reservationService.updateReservationStatus(
                        8L,
                        ReservationStatus.CONFIRMED
                )
        ).thenReturn(response);

        mockMvc.perform(
                        patch("/reservation/8/status")
                                .param(
                                        "status",
                                        "CONFIRMED"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(
                        jsonPath("$.customerName")
                                .value("john")
                )
                .andExpect(
                        jsonPath("$.roomType")
                                .value("Deluxe")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("CONFIRMED")
                );
    }


    @Test
    void updateReservationStatus_ShouldReturnBadRequest_WhenTransitionIsInvalid()
            throws Exception {

        when(
                reservationService.updateReservationStatus(
                        8L,
                        ReservationStatus.CONFIRMED
                )
        ).thenThrow(
                new InvalidReservationStatusException(
                        "Cancelled reservation status cannot be changed"
                )
        );

        mockMvc.perform(
                        patch("/reservation/8/status")
                                .param(
                                        "status",
                                        "CONFIRMED"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
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
    void createReservation_ShouldReturnDates_WhenDatesAreValid()
            throws Exception {

        ReservationRequest request =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25)
                );

        ReservationResponse response =
                new ReservationResponse(
                        1L,
                        "David",
                        "Deluxe",
                        ReservationStatus.PENDING,
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 25)
                );

        when(
                reservationService.createReservation(
                        any(ReservationRequest.class),
                        eq("john")
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/reservation")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("David"))
                .andExpect(jsonPath("$.roomType").value("Deluxe"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.checkInDate").value("2026-08-20"))
                .andExpect(jsonPath("$.checkOutDate").value("2026-08-25"));
    }


    @Test
    void createReservation_ShouldReturnBadRequest_WhenDatesAreMissing()
            throws Exception {

        ReservationRequest request =
                new ReservationRequest();

        request.setCustomerName("David");
        request.setRoomType("Deluxe");


                mockMvc.perform(
                                post("/reservation")
                                        .principal(
                                                new UsernamePasswordAuthenticationToken(
                                                        "john",
                                                        null,
                                                        List.of(
                                                                new SimpleGrantedAuthority(
                                                                        "ROLE_USER"
                                                                )
                                                        )
                                                )
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(request)
                                        )
                        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.checkInDate")
                                .value(
                                        "Check-in date is required"
                                )
                )
                .andExpect(
                        jsonPath("$.fieldErrors.checkOutDate")
                                .value(
                                        "Check-out date is required"
                                )
                );

    }


    @Test
    void createReservation_ShouldReturnConflict_WhenReservationOverlaps()
            throws Exception {

        ReservationRequest request =
                new ReservationRequest(
                        "John",
                        "Deluxe",
                        LocalDate.of(2026, 8, 22),
                        LocalDate.of(2026, 8, 27)
                );

        when(
                reservationService.createReservation(
                        any(ReservationRequest.class),
                        eq("john")
                )
        ).thenThrow(
                new ReservationConflictException(
                        "Reservation overlaps with an existing booking"
                )
        );

        mockMvc.perform(
                        post("/reservation")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflict")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Reservation overlaps with an existing booking"
                                )
                );
    }


    @Test
    void updateReservation_ShouldReturnConflict_WhenUpdatedDatesOverlap()
            throws Exception {

        Long reservationId = 1L;

        ReservationRequest request =
                new ReservationRequest(
                        "David",
                        "Deluxe",
                        LocalDate.of(2026, 8, 22),
                        LocalDate.of(2026, 8, 27)
                );

        when(
                reservationService.updateReservation(
                        eq(reservationId),
                        any(ReservationRequest.class),
                        eq("john")
                )
        ).thenThrow(
                new ReservationConflictException(
                        "Reservation overlaps with an existing booking"
                )
        );

        mockMvc.perform(
                        put(
                                "/reservation/{id}",
                                reservationId
                        )
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflict")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Reservation overlaps with an existing booking"
                                )
                );
    }
    @Test
    void getMyReservations_ShouldReturnReservationsForAuthenticatedUser()
            throws Exception {

        ReservationResponse reservation1 =
                new ReservationResponse(
                        1L,
                        "John",
                        "Deluxe"
                );

        ReservationResponse reservation2 =
                new ReservationResponse(
                        2L,
                        "John",
                        "Suite"
                );

        List<ReservationResponse> reservations =
                List.of(
                        reservation1,
                        reservation2
                );

        when(
                reservationService.getReservationsByUsername(
                        "john"
                )
        ).thenReturn(reservations);

        mockMvc.perform(
                        get("/reservation/my")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].customerName")
                                .value("John")
                )
                .andExpect(
                        jsonPath("$[0].roomType")
                                .value("Deluxe")
                )
                .andExpect(
                        jsonPath("$[1].id")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[1].customerName")
                                .value("John")
                )
                .andExpect(
                        jsonPath("$[1].roomType")
                                .value("Suite")
                );

        verify(
                reservationService
        ).getReservationsByUsername(
                "john"
        );
    }
    @Test
    void cancelReservation_ShouldReturnCancelledReservation()
            throws Exception {

        ReservationResponse response =
                new ReservationResponse(
                        1L,
                        "John",
                        "Deluxe",
                        ReservationStatus.CANCELLED
                );

        when(
                reservationService.cancelReservation(
                        1L,
                        "john"
                )
        ).thenReturn(response);

        mockMvc.perform(
                        patch("/reservation/1/cancel")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.customerName")
                                .value("John")
                )
                .andExpect(
                        jsonPath("$.roomType")
                                .value("Deluxe")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("CANCELLED")
                );

        verify(
                reservationService
        ).cancelReservation(
                1L,
                "john"
        );
    }
    @Test
    void cancelReservation_ShouldReturnForbidden_WhenUserIsNotOwner()
            throws Exception {

        when(
                reservationService.cancelReservation(
                        2L,
                        "john"
                )
        ).thenThrow(
                new AccessDeniedException(
                        "You are not allowed to cancel this reservation"
                )
        );

        mockMvc.perform(
                        patch("/reservation/2/cancel")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
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

        when(
                reservationService.cancelReservation(
                        3L,
                        "john"
                )
        ).thenThrow(
                new InvalidReservationStatusException(
                        "Reservation is already cancelled"
                )
        );

        mockMvc.perform(
                        patch("/reservation/3/cancel")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                "john",
                                                null,
                                                List.of(
                                                        new SimpleGrantedAuthority(
                                                                "ROLE_USER"
                                                        )
                                                )
                                        )
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

}