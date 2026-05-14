package com.doctorapp.backend;

import com.doctorapp.backend.entity.Patient;
import com.doctorapp.backend.entity.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @PostMapping("/patient/register")
    public ResponseEntity<?> register(@RequestBody Patient patient) {
        if (patient.getEmail() != null && patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }
        return ResponseEntity.ok(patientRepository.save(patient));
    }

    @PostMapping("/patient/login")
    public ResponseEntity<?> login(@RequestBody Patient loginRequest) {
        java.util.Optional<Patient> patient = patientRepository.findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword());
        if (patient.isPresent()) {
            return ResponseEntity.ok(patient.get());
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
    }
}
