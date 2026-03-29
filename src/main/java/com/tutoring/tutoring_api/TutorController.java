package com.tutoring.tutoring_api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api")
public class TutorController {

    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public TutorController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/tutors")
    public List<Map<String, Object>> getTutors() {
        return jdbc.queryForList("SELECT * FROM tutors");
    }

    @GetMapping("/tutors/{id}")
    public Map<String, Object> getTutorById(@PathVariable int id) {
        return jdbc.queryForMap("SELECT * FROM tutors WHERE id = ?", id);
    }

    @GetMapping("/tutors/{id}/subjects")
    public List<Map<String, Object>> getTutorSubjects(@PathVariable int id) {
        return jdbc.queryForList("""
            SELECT s.* FROM subjects s
            JOIN tutor_subjects ts ON ts.subject_id = s.id
            WHERE ts.tutor_id = ?
        """, id);
    }

    @GetMapping("/subjects")
    public List<Map<String, Object>> getSubjects() {
        return jdbc.queryForList("SELECT * FROM subjects");
    }

    @GetMapping("/bookings")
    public List<Map<String, Object>> getBookings() {
        return jdbc.queryForList("""
            SELECT b.*, 
                   t.first_name as tutor_first, t.last_name as tutor_last,
                   s.first_name as student_first, s.last_name as student_last,
                   sub.name as subject_name
            FROM bookings b
            JOIN tutors t ON t.id = b.tutor_id
            JOIN students s ON s.id = b.student_id
            JOIN subjects sub ON sub.id = b.subject_id
        """);
    }
    @GetMapping("/students")
    public List<Map<String, Object>> getStudents() {
        return jdbc.queryForList("SELECT id, first_name, last_name, email, grade_level, created_at FROM students");
    }

    @GetMapping("/students/{id}/bookings")
    public List<Map<String, Object>> getStudentBookings(@PathVariable int id) {
        return jdbc.queryForList("""
        SELECT b.*,
               t.first_name as tutor_first, t.last_name as tutor_last,
               s.name as subject_name
        FROM bookings b
        JOIN tutors t ON t.id = b.tutor_id
        JOIN subjects s ON s.id = b.subject_id
        WHERE b.student_id = ?
    """, id);
    }

    @PostMapping("/bookings")
    public String createBooking(@RequestBody Map<String, Object> body) {
        jdbc.update("""
            INSERT INTO bookings (tutor_id, student_id, subject_id, scheduled_at, duration_minutes, status)
            VALUES (?, ?, ?, ?::timestamp, ?, 'pending')
        """,
                body.get("tutor_id"),
                body.get("student_id"),
                body.get("subject_id"),
                body.get("scheduled_at"),
                body.get("duration_minutes")
        );
        return "{\"message\": \"Booking created successfully\"}";
    }
    @PostMapping("/auth/login")
    public org.springframework.http.ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        String email = body.get("email").toString();
        String password = body.get("password").toString();

        try {
            Map<String, Object> student = jdbc.queryForMap(
                    "SELECT * FROM students WHERE email = ?", email
            );

            String storedHash = student.get("password_hash").toString();
            if (bcrypt.matches(password, storedHash)) {
                student.remove("password_hash");
                return org.springframework.http.ResponseEntity.ok(student);
            } else {
                return org.springframework.http.ResponseEntity
                        .status(401)
                        .body("{\"error\": \"Invalid password\"}");
            }
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity
                    .status(401)
                    .body("{\"error\": \"Student not found\"}");
        }
    }

    @GetMapping("/auth/hashgen")
    public String generateHash() {
        return bcrypt.encode("password123");
    }
}
