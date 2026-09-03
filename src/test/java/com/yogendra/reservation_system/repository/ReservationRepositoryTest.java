package com.yogendra.reservation_system.repository;

import com.yogendra.reservation_system.model.Reservation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.test.context.ActiveProfiles;
import com.yogendra.reservation_system.model.ReservationStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
@DataJpaTest
@ActiveProfiles("test")
class ReservationRepositoryTest {
    @Test
    void save_ShouldPersistReservation() {

        Reservation reservation = new Reservation();

        reservation.setCustomerName("David");
        reservation.setRoomType("Deluxe");

        Reservation savedReservation =
                reservationRepository.save(reservation);

        assertNotNull(savedReservation);
        assertNotNull(savedReservation.getId());

        assertEquals(
                "David",
                savedReservation.getCustomerName()
        );

        assertEquals(
                "Deluxe",
                savedReservation.getRoomType()
        );
    }
    @Test
    void findById_ShouldReturnReservation() {

        Reservation reservation = new Reservation();
        reservation.setCustomerName("David");
        reservation.setRoomType("Deluxe");

        Reservation savedReservation =
                reservationRepository.save(reservation);

        Optional<Reservation> foundReservation =
                reservationRepository.findById(savedReservation.getId());

        assertTrue(foundReservation.isPresent());

        assertEquals(
                "David",
                foundReservation.get().getCustomerName()
        );

        assertEquals(
                "Deluxe",
                foundReservation.get().getRoomType()
        );
    }
    @Test
    void delete_ShouldRemoveReservation() {

        Reservation reservation = new Reservation();
        reservation.setCustomerName("Roy");
        reservation.setRoomType("Suite");

        Reservation savedReservation =
                reservationRepository.save(reservation);

        Long id = savedReservation.getId();

        reservationRepository.deleteById(id);

        Optional<Reservation> deletedReservation =
                reservationRepository.findById(id);

        assertTrue(deletedReservation.isEmpty());
    }
    @Test
    void findByCustomerNameContainingIgnoreCase_ShouldReturnMatchingReservations() {

        Reservation r1 = new Reservation();
        r1.setCustomerName("David");
        r1.setRoomType("Deluxe");

        Reservation r2 = new Reservation();
        r2.setCustomerName("Daniel");
        r2.setRoomType("Suite");

        Reservation r3 = new Reservation();
        r3.setCustomerName("Roy");
        r3.setRoomType("Standard");

        reservationRepository.save(r1);
        reservationRepository.save(r2);
        reservationRepository.save(r3);

        List<Reservation> results =
                reservationRepository
                        .findByCustomerNameContainingIgnoreCase("da");

        assertEquals(2, results.size());

        assertTrue(
                results.stream()
                        .anyMatch(r -> r.getCustomerName().equals("David"))
        );

        assertTrue(
                results.stream()
                        .anyMatch(r -> r.getCustomerName().equals("Daniel"))
        );
    }
    @Test
    void findByRoomTypeContainingIgnoreCase_ShouldReturnMatchingReservations() {

        Reservation r1 = new Reservation();
        r1.setCustomerName("David");
        r1.setRoomType("Deluxe");

        Reservation r2 = new Reservation();
        r2.setCustomerName("Roy");
        r2.setRoomType("Deluxe");

        Reservation r3 = new Reservation();
        r3.setCustomerName("Mike");
        r3.setRoomType("Standard");

        reservationRepository.save(r1);
        reservationRepository.save(r2);
        reservationRepository.save(r3);

        List<Reservation> results =
                reservationRepository
                        .findByRoomTypeContainingIgnoreCase("del");

        assertEquals(2, results.size());

        assertTrue(
                results.stream()
                        .allMatch(r ->
                                r.getRoomType()
                                        .equalsIgnoreCase("Deluxe"))
        );
    }
    @Test
    void findByCustomerNameAndRoomType_ShouldReturnMatchingReservations() {

        Reservation r1 = new Reservation();
        r1.setCustomerName("David");
        r1.setRoomType("Deluxe");

        Reservation r2 = new Reservation();
        r2.setCustomerName("David");
        r2.setRoomType("Suite");

        Reservation r3 = new Reservation();
        r3.setCustomerName("Roy");
        r3.setRoomType("Deluxe");

        reservationRepository.save(r1);
        reservationRepository.save(r2);
        reservationRepository.save(r3);

        List<Reservation> results =
                reservationRepository
                        .findByCustomerNameContainingIgnoreCaseAndRoomTypeContainingIgnoreCase(
                                "dav",
                                "del"
                        );

        assertEquals(1, results.size());

        assertEquals(
                "David",
                results.get(0).getCustomerName()
        );

        assertEquals(
                "Deluxe",
                results.get(0).getRoomType()
        );
    }
    @Test
    void findAll_ShouldReturnPagedAndSortedReservations() {

        Reservation r1 = new Reservation();
        r1.setCustomerName("Charlie");
        r1.setRoomType("Deluxe");

        Reservation r2 = new Reservation();
        r2.setCustomerName("Alice");
        r2.setRoomType("Suite");

        Reservation r3 = new Reservation();
        r3.setCustomerName("Bob");
        r3.setRoomType("Standard");

        reservationRepository.save(r1);
        reservationRepository.save(r2);
        reservationRepository.save(r3);

        PageRequest pageable = PageRequest.of(
                0,
                2,
                Sort.by("customerName").ascending()
        );

        Page<Reservation> page =
                reservationRepository.findAll(pageable);

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());

        assertEquals(
                "Alice",
                page.getContent().get(0).getCustomerName()
        );

        assertEquals(
                "Bob",
                page.getContent().get(1).getCustomerName()
        );
    }
    @Test
    void findOverlappingReservations_ShouldReturnMatchingReservation() {

        Reservation existingReservation = new Reservation();
        existingReservation.setCustomerName("David");
        existingReservation.setRoomType("Deluxe");
        existingReservation.setCheckInDate(
                LocalDate.of(2026, 8, 20)
        );
        existingReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 25)
        );

        reservationRepository.save(existingReservation);

        List<Reservation> results =
                reservationRepository
                        .findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                "Deluxe",
                                LocalDate.of(2026, 8, 27),
                                LocalDate.of(2026, 8, 22)
                        );

        assertEquals(1, results.size());

        assertEquals(
                "David",
                results.get(0).getCustomerName()
        );

        assertEquals(
                "Deluxe",
                results.get(0).getRoomType()
        );
    }
    @Test
    void findOverlappingReservations_ShouldReturnEmpty_WhenDatesDoNotOverlap() {

        Reservation existingReservation = new Reservation();
        existingReservation.setCustomerName("David");
        existingReservation.setRoomType("Deluxe");
        existingReservation.setCheckInDate(
                LocalDate.of(2026, 8, 20)
        );
        existingReservation.setCheckOutDate(
                LocalDate.of(2026, 8, 25)
        );

        reservationRepository.save(existingReservation);

        List<Reservation> results =
                reservationRepository
                        .findByRoomTypeIgnoreCaseAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                                "Deluxe",
                                LocalDate.of(2026, 8, 30),
                                LocalDate.of(2026, 8, 25)
                        );

        assertTrue(results.isEmpty());
    }
    @Test
    void saveReservation_ShouldPopulateCreatedAtAndUpdatedAt() {

        Reservation reservation = new Reservation();

        reservation.setCustomerName("David");
        reservation.setRoomType("Deluxe");
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCheckInDate(
                LocalDate.of(2026, 9, 10)
        );
        reservation.setCheckOutDate(
                LocalDate.of(2026, 9, 15)
        );

        Reservation savedReservation =
                reservationRepository.saveAndFlush(
                        reservation
                );

        assertNotNull(
                savedReservation.getCreatedAt()
        );

        assertNotNull(
                savedReservation.getUpdatedAt()
        );

        assertEquals(
                savedReservation.getCreatedAt(),
                savedReservation.getUpdatedAt()
        );
    }
    @Test
    void updateReservation_ShouldKeepCreatedAtAndChangeUpdatedAt()
            throws InterruptedException {

        Reservation reservation = new Reservation();

        reservation.setCustomerName("David");
        reservation.setRoomType("Deluxe");
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCheckInDate(
                LocalDate.of(2026, 9, 10)
        );
        reservation.setCheckOutDate(
                LocalDate.of(2026, 9, 15)
        );

        Reservation savedReservation =
                reservationRepository.saveAndFlush(
                        reservation
                );

        LocalDateTime originalCreatedAt =
                savedReservation.getCreatedAt();

        LocalDateTime originalUpdatedAt =
                savedReservation.getUpdatedAt();

        Thread.sleep(10);

        savedReservation.setCustomerName(
                "David Updated"
        );

        Reservation updatedReservation =
                reservationRepository.saveAndFlush(
                        savedReservation
                );

        assertEquals(
                originalCreatedAt,
                updatedReservation.getCreatedAt()
        );

        assertNotEquals(
                originalUpdatedAt,
                updatedReservation.getUpdatedAt()
        );

        assertTrue(
                updatedReservation
                        .getUpdatedAt()
                        .isAfter(originalUpdatedAt)
        );
    }


    @Autowired
    private ReservationRepository reservationRepository;

}