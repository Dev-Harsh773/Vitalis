package com.doctorapp.backend;

import com.doctorapp.backend.entity.Doctor;
import com.doctorapp.backend.entity.DoctorRepository;
import com.doctorapp.backend.entity.Booking;
import com.doctorapp.backend.entity.BookingRepository;
import com.doctorapp.backend.entity.Slot;
import com.doctorapp.backend.entity.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private com.doctorapp.backend.entity.PatientRepository patientRepository;

    @PostMapping("/doctor/register")
    public org.springframework.http.ResponseEntity<?> register(@RequestBody Doctor doctor) {
        if (doctor.getEmail() != null && doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", "Email already exists"));
        }
        return org.springframework.http.ResponseEntity.ok(doctorRepository.save(doctor));
    }

    @PostMapping("/doctor/login")
    public org.springframework.http.ResponseEntity<?> login(@RequestBody Doctor loginRequest) {
        Optional<Doctor> doctor = doctorRepository.findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword());
        if (doctor.isPresent()) {
            return org.springframework.http.ResponseEntity.ok(doctor.get());
        } else {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid email or password"));
        }
    }

    @GetMapping("/doctors")
    public List<Doctor> getDoctors(@RequestParam String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    @GetMapping("/doctors/all")
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @GetMapping("/doctor/bookings/{doctorId}")
    public org.springframework.http.ResponseEntity<?> getDoctorBookings(@PathVariable Long doctorId) {
        Doctor doc = doctorRepository.findById(doctorId).orElse(null);
        if (doc == null) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", "Doctor not found"));
        }

        List<Slot> slots = slotRepository.findByDoctorId(doctorId);

        List<Map<String, Object>> slotsArr = new ArrayList<>();
        for (Slot slot : slots) {
            Map<String, Object> sObj = new HashMap<>();
            sObj.put("time", slot.getTime());
            sObj.put("totalBookings", slot.getBookedCount());
            
            List<Booking> slotBookings = bookingRepository.findBySlotId(slot.getId());
            List<Map<String, Object>> patients = new ArrayList<>();
            for (Booking b : slotBookings) {
                com.doctorapp.backend.entity.Patient p = patientRepository.findById(b.getPatientId()).orElse(null);
                if (p != null) {
                    Map<String, Object> pInfo = new HashMap<>();
                    pInfo.put("id", p.getId());
                    pInfo.put("name", p.getName());
                    patients.add(pInfo);
                }
            }
            sObj.put("patients", patients);
            slotsArr.add(sObj);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("doctorName", doc.getName());
        response.put("specialization", doc.getSpecialization());
        response.put("slots", slotsArr);

        return org.springframework.http.ResponseEntity.ok(response);
    }
}
