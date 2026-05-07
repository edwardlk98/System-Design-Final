package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HospitalController {

    private final Map<String, User> users = new HashMap<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Appointment> appointments = new ArrayList<>();

    @PostConstruct
    public void init() {
        doctors.add(new Doctor(1, "Dr. Maya Patel", "Cardiology", "Heart care specialist with 12 years of experience."));
        doctors.add(new Doctor(2, "Dr. Kevin Thompson", "Neurology", "Expert in brain and nervous system treatment."));
        doctors.add(new Doctor(3, "Dr. Aisha Rahman", "Pediatrics", "Child health doctor with a caring bedside manner."));
        doctors.add(new Doctor(4, "Dr. Samuel Kim", "Orthopedics", "Bone and joint specialist for adults and seniors."));
        doctors.add(new Doctor(5, "Dr. Elena Garcia", "Dermatology", "Skin and allergy doctor for comprehensive skin care."));

        // Create doctor user accounts
        users.put("edward", new User("edward", "Dr. Maya Patel", "test", "doctor"));

        
        users.put("admin", new User("admin", "Hospital Admin", "admin123", "admin"));
    }

    @GetMapping("/doctors")
    public List<Doctor> getDoctors() {
        return doctors;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse> signUp(@RequestBody SignUpRequest request) {
        if (request.username == null || request.username.isBlank() || request.password == null || request.password.isBlank() || request.fullName == null || request.fullName.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "All signup fields are required."));
        }
        if (users.containsKey(request.username)) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "This username is already taken."));
        }
        users.put(request.username, new User(request.username, request.fullName, request.password));
        return ResponseEntity.ok(new ApiResponse(true, "Sign up successful. Welcome, " + request.fullName + "!"));
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest request) {
        if (request.username == null || request.username.isBlank() || request.password == null || request.password.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username and password are required."));
        }
        User user = users.get(request.username);
        if (user == null || !user.password.equals(request.password)) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid username or password."));
        }
        return ResponseEntity.ok(new SignInResponse(user.fullName, "Welcome back, " + user.fullName + "!", user.userType));
    }

    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse> bookAppointment(@RequestBody AppointmentRequest request) {
        if (request.username == null || request.username.isBlank() || request.doctorId <= 0 || request.date == null || request.date.isBlank() || request.time == null || request.time.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Complete appointment details are required."));
        }
        if (!users.containsKey(request.username)) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Please sign in before booking an appointment."));
        }
        Doctor doctor = doctors.stream().filter(d -> d.id == request.doctorId).findFirst().orElse(null);
        if (doctor == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Selected doctor does not exist."));
        }
        
        boolean isSlotAvailable = true;
        if (doctor.availability != null && !doctor.availability.isEmpty()) {
            isSlotAvailable = doctor.availability.stream()
                .anyMatch(slot -> slot.date.equals(request.date) && slot.time.equals(request.time) && slot.isAvailable);
        }
        
        if (!isSlotAvailable) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "This time slot is not available. Please choose a different time."));
        }
        
        appointments.add(new Appointment(request.username, doctor.id, request.date, request.time));
        return ResponseEntity.ok(new ApiResponse(true, "Appointment booked for " + doctor.name + " on " + request.date + " at " + request.time + "."));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getAppointments(@RequestParam String username) {
        List<AppointmentResponse> userAppointments = appointments.stream()
            .filter(a -> a.username.equals(username))
            .map(a -> {
                Doctor doctor = doctors.stream().filter(d -> d.id == a.doctorId).findFirst().orElse(null);
                String doctorName = doctor != null ? doctor.name : "Unknown Doctor";
                String department = doctor != null ? doctor.department : "Unknown";
                return new AppointmentResponse(a.username, doctorName, a.doctorId, a.date, a.time, department);
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(userAppointments);
    }

    @PostMapping("/doctors/availability")
    public ResponseEntity<ApiResponse> setDoctorAvailability(@RequestBody AvailabilityRequest request) {
        if (request.username == null || request.username.isBlank() || request.doctorId <= 0) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username and doctor ID are required."));
        }
        User user = users.get(request.username);
        if (user == null || !user.userType.equals("doctor")) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Only doctors can set availability."));
        }
        Doctor doctor = doctors.stream().filter(d -> d.id == request.doctorId).findFirst().orElse(null);
        if (doctor == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Doctor not found."));
        }
        doctor.availability = request.availability;
        return ResponseEntity.ok(new ApiResponse(true, "Availability updated successfully."));
    }

    @GetMapping("/doctors/availability")
    public ResponseEntity<List<DoctorAvailabilityResponse>> getDoctorAvailability(@RequestParam int doctorId) {
        Doctor doctor = doctors.stream().filter(d -> d.id == doctorId).findFirst().orElse(null);
        if (doctor == null) {
            return ResponseEntity.badRequest().build();
        }
        List<DoctorAvailabilityResponse> response = new ArrayList<>();
        if (doctor.availability != null && !doctor.availability.isEmpty()) {
            doctor.availability.forEach(slot -> {
                response.add(new DoctorAvailabilityResponse(doctor.name, slot.date, slot.time, slot.isAvailable));
            });
        }
        return ResponseEntity.ok(response);
    }

    static class User {
        public String username;
        public String fullName;
        public String password;
        public String userType; 

        public User(String username, String fullName, String password) {
            this.username = username;
            this.fullName = fullName;
            this.password = password;
            this.userType = "patient";
        }

        public User(String username, String fullName, String password, String userType) {
            this.username = username;
            this.fullName = fullName;
            this.password = password;
            this.userType = userType;
        }
    }

    static class Doctor {
        public int id;
        public String name;
        public String department;
        public String bio;
        public List<AvailabilitySlot> availability;

        public Doctor(int id, String name, String department, String bio) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.bio = bio;
            this.availability = new ArrayList<>();
        }
    }

    static class Appointment {
        public String username;
        public int doctorId;
        public String date;
        public String time;

        public Appointment(String username, int doctorId, String date, String time) {
            this.username = username;
            this.doctorId = doctorId;
            this.date = date;
            this.time = time;
        }
    }

    static class SignUpRequest {
        public String username;
        public String fullName;
        public String password;
    }

    static class SignInRequest {
        public String username;
        public String password;
    }

    static class AppointmentRequest {
        public String username;
        public int doctorId;
        public String date;
        public String time;
    }

    static class SignInResponse {
        public String fullName;
        public String welcomeMessage;
        public String userType;

        public SignInResponse(String fullName, String welcomeMessage) {
            this.fullName = fullName;
            this.welcomeMessage = welcomeMessage;
            this.userType = "patient";
        }

        public SignInResponse(String fullName, String welcomeMessage, String userType) {
            this.fullName = fullName;
            this.welcomeMessage = welcomeMessage;
            this.userType = userType;
        }
    }

    static class ApiResponse {
        public boolean success;
        public String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    static class AvailabilitySlot {
        public String date;
        public String time;
        public boolean isAvailable;

        public AvailabilitySlot(String date, String time, boolean isAvailable) {
            this.date = date;
            this.time = time;
            this.isAvailable = isAvailable;
        }
    }

    static class AvailabilityRequest {
        public String username;
        public int doctorId;
        public List<AvailabilitySlot> availability;
    }

    static class DoctorAvailabilityResponse {
        public String doctorName;
        public String date;
        public String time;
        public boolean isAvailable;

        public DoctorAvailabilityResponse(String doctorName, String date, String time, boolean isAvailable) {
            this.doctorName = doctorName;
            this.date = date;
            this.time = time;
            this.isAvailable = isAvailable;
        }
    }

    static class AppointmentResponse {
        public String username;
        public String doctorName;
        public int doctorId;
        public String date;
        public String time;
        public String department;

        public AppointmentResponse(String username, String doctorName, int doctorId, String date, String time, String department) {
            this.username = username;
            this.doctorName = doctorName;
            this.doctorId = doctorId;
            this.date = date;
            this.time = time;
            this.department = department;
        }
    }
}
