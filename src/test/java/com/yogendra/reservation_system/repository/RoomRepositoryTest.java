package com.yogendra.reservation_system.repository;

import com.yogendra.reservation_system.model.Room;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void saveAndFindByRoomNumber_ShouldReturnRoom() {

        Room room = new Room(
                "201",
                "Deluxe",
                true
        );

        roomRepository.save(room);

        Optional<Room> result =
                roomRepository.findByRoomNumber("201");

        assertTrue(result.isPresent());

        assertEquals(
                "201",
                result.get().getRoomNumber()
        );

        assertEquals(
                "Deluxe",
                result.get().getRoomType()
        );

        assertTrue(
                result.get().isAvailable()
        );
    }
    @Test
    void findByRoomTypeIgnoreCase_ShouldReturnMatchingRooms() {

        Room room1 = new Room(
                "201",
                "Deluxe",
                true
        );

        Room room2 = new Room(
                "202",
                "Deluxe",
                true
        );

        Room room3 = new Room(
                "301",
                "Suite",
                true
        );

        roomRepository.save(room1);
        roomRepository.save(room2);
        roomRepository.save(room3);

        List<Room> results =
                roomRepository.findByRoomTypeIgnoreCase("deluxe");

        assertEquals(2, results.size());

        assertTrue(
                results.stream()
                        .allMatch(
                                room -> room.getRoomType()
                                        .equalsIgnoreCase("Deluxe")
                        )
        );
    }
}