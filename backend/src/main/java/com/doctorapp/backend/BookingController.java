package com.doctorapp.backend;

import com.doctorapp.backend.entity.Booking;
import com.doctorapp.backend.entity.BookingRepository;
import com.doctorapp.backend.entity.Slot;
import com.doctorapp.backend.entity.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SlotRepository slotRepository;

    @PostMapping("/book")
    public Booking book(@RequestBody Booking booking) {
        // Save the booking
        Booking saved = bookingRepository.save(booking);

        // Increase booked_count on the slot
        Optional<Slot> slotOpt = slotRepository.findById(booking.getSlotId());
        if (slotOpt.isPresent()) {
            Slot slot = slotOpt.get();
            slot.setBookedCount(slot.getBookedCount() + 1);
            slotRepository.save(slot);
        }

        return saved;
    }
}
