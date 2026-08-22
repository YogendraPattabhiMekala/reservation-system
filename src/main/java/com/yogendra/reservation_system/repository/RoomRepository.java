package com.yogendra.reservation_system.repository;

import com.yogendra.reservation_system.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByRoomTypeIgnoreCase(String roomType);
}