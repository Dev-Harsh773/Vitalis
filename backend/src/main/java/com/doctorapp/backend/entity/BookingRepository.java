package com.doctorapp.backend.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByDoctorId(Long doctorId);
    List<Booking> findBySlotId(Long slotId);
}
