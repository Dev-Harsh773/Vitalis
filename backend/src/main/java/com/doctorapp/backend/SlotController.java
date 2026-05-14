package com.doctorapp.backend;

import com.doctorapp.backend.entity.Slot;
import com.doctorapp.backend.entity.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
public class SlotController {

    @Autowired
    private SlotRepository slotRepository;

    @PostMapping("/create")
    public Slot create(@RequestBody Slot slot) {
        return slotRepository.save(slot);
    }

    @GetMapping("/{doctorId}")
    public List<Slot> getSlots(@PathVariable Long doctorId) {
        return slotRepository.findByDoctorId(doctorId);
    }
}
