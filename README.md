# Vitalis — Doctor Appointment Booking App

**Vitalis** is a doctor appointment booking Android application that helps patients select symptoms based on body regions, find the right specialist nearby, and book consultation slots. Doctors can register, set their clinic location, create available time slots, and manage patient bookings.

> The name **Vitalis** means “life-giving” or “related to life,” representing the app’s goal of making healthcare access easier and faster.

---

## 📱 Screenshots

### Role Selection & Login
<p align="center">
  <img width="720" height="1600" alt="1" src="https://github.com/user-attachments/assets/29110ae4-a186-42d6-96da-df1a8bca7aed" />
  <img width="1080" height="2400" alt="2" src="https://github.com/user-attachments/assets/89698ad9-2696-491d-9d61-45919152827a" />
</p>

### Doctor Flow
<p align="center">
  <img width="720" height="1600" alt="3" src="https://github.com/user-attachments/assets/2c633e27-f47e-4eb7-b671-31433973bf3a" />
  <img width="720" height="1600" alt="4" src="https://github.com/user-attachments/assets/b9ca513c-03d3-47d1-8994-d08e5235a33a" />
</p>

### Patient Flow
<p align="center">
  <img width="720" height="1600" alt="6" src="https://github.com/user-attachments/assets/cb8868f0-5044-4882-aec9-0c6d3c2c8cd1" />
  <img width="1080" height="2400" alt="7" src="https://github.com/user-attachments/assets/e972c44d-919c-4640-9c7d-a82a75c6c8d1" />
  <img width="720" height="1600" alt="8" src="https://github.com/user-attachments/assets/b862f0e5-ca7e-41a1-acde-c4150b24617c" />
  <img width="720" height="1600" alt="9" src="https://github.com/user-attachments/assets/f95e6548-597d-460e-b706-4b1231e149fd" />
</p>

---

## 🚀 Features

### Patient Features

- Patient login and registration
- Interactive body symptom selection screen
- Disease suggestions based on selected body part
- Automatic specialist matching
- Fetches doctors based on specialization
- Sorts doctors by distance using current location
- Displays nearby specialist doctors
- Allows patients to book available slots

### Doctor Features

- Doctor login and registration
- Doctor specialization selection
- Clinic location selection using Google Maps
- Doctor dashboard with profile details
- Create consultation time slots
- View scheduled slots and booking counts
- View patient bookings for each slot

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Mobile App | Android Java |
| Backend | Spring Boot 4.0.5 |
| Programming Language | Java 17 |
| Database | MySQL |
| Maps | Google Maps SDK |
| Location | Google Fused Location API |

---

## 🔄 Application Flow

### Patient Side

1. User opens the app.
2. Selects **Patient** from the role selection screen.
3. Logs in or registers as a patient.
4. Opens the body selection screen.
5. Selects a body part such as Head, Eye, Nose, Mouth, Neck, Chest, Abdomen, Arms, or Legs.
6. Chooses a disease related to the selected body part.
7. App automatically maps the disease to the correct specialist.
8. Backend fetches doctors with the matching specialization.
9. Doctors are sorted based on distance from the patient’s current location.
10. Patient selects a doctor and books an available consultation slot.
11. Booking is stored in the database.

### Doctor Side

1. User selects **Doctor** from the role selection screen.
2. Doctor logs in or registers.
3. During registration, doctor enters name, specialization, email, password, and clinic location.
4. After login, doctor lands on the dashboard.
5. Doctor can view profile, specialization, created slots, and booking count.
6. Doctor can create new consultation slots.
7. Bookings made by patients are visible on the doctor dashboard.

---

## 🧠 Disease to Specialist Mapping

| Body Part | Disease | Specialist |
|---|---|---|
| Eye | Eye Strain, Conjunctivitis | Ophthalmologist |
| Nose | Sinusitis, Allergies | ENT |
| Mouth / Teeth | Toothache, Cavities | Dentist |
| Neck | Thyroid Issue | Endocrinologist |
| Chest | Heart Disease | Cardiologist |
| Chest | Asthma | Pulmonologist |
| Abdomen | Acidity, IBS | Gastroenterologist |
| Abdomen | Kidney Stone, UTI | Urologist |
| Arms / Legs | Fracture, Arthritis | Orthopedic |
| Head | Migraine | Neurologist |

---

## 🌐 Backend API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/doctor/register` | Register a new doctor |
| POST | `/doctor/login` | Doctor login |
| GET | `/doctors/all` | Get all doctors |
| GET | `/doctors?specialization=X` | Get doctors by specialization |
| GET | `/doctor/bookings/{doctorId}` | Get doctor's bookings with patient info |
| POST | `/patient/register` | Register a new patient |
| POST | `/patient/login` | Patient login |
| POST | `/slots/create` | Create a doctor time slot |
| GET | `/slots/{doctorId}` | Get all slots of a doctor |
| POST | `/book` | Book a consultation slot |
| GET | `/test` | Health check endpoint |

---

## 🗄 Database Structure

### doctors table

| Field | Description |
|---|---|
| id | Doctor ID |
| name | Doctor name |
| specialization | Doctor specialization |
| email | Doctor email |
| password | Doctor password |
| latitude | Clinic latitude |
| longitude | Clinic longitude |

### patients table

| Field | Description |
|---|---|
| id | Patient ID |
| name | Patient name |
| email | Patient email |
| password | Patient password |

### slots table

| Field | Description |
|---|---|
| id | Slot ID |
| doctorId | Doctor ID |
| time | Slot timing |
| bookedCount | Number of bookings |

### bookings table

| Field | Description |
|---|---|
| id | Booking ID |
| doctorId | Doctor ID |
| patientId | Patient ID |
| slotId | Slot ID |

---

## 📁 Project Structure

```txt
Vitalis/
├── android/
│   └── app/src/main/
│       ├── java/com/doctorapp/android/
│       │   ├── MainActivity.java
│       │   ├── LoginActivity.java
│       │   ├── RegisterActivity.java
│       │   ├── BodySelectionActivity.java
│       │   ├── DoctorDashboardActivity.java
│       │   ├── SlotCreationActivity.java
│       │   └── MapSelectionActivity.java
│       │
│       └── res/
│           ├── layout/
│           ├── drawable/
│           └── values/
│
└── backend/
    └── src/main/java/com/doctorapp/backend/
        ├── BackendApplication.java
        ├── DoctorController.java
        ├── PatientController.java
        ├── SlotController.java
        ├── BookingController.java
        └── entity/
            ├── Doctor.java
            ├── DoctorRepository.java
            ├── Patient.java
            ├── PatientRepository.java
            ├── Slot.java
            ├── SlotRepository.java
            ├── Booking.java
            └── BookingRepository.java
