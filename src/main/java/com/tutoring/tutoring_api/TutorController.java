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
        return jdbc.queryForList("SELECT * FROM tutors WHERE status = 'approved'");
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

    @GetMapping("/checkpoints/{tutorId}")
    public List<Map<String, Object>> getCheckpointsByTutor(@PathVariable int tutorId) {
        return jdbc.queryForList(
                "SELECT * FROM progress_checkpoints WHERE tutor_id = ? ORDER BY created_at",
                tutorId);
    }

    @GetMapping("/checkpoints/student/{studentId}")
    public List<Map<String, Object>> getCheckpointsByStudent(@PathVariable int studentId) {
        return jdbc.queryForList(
                "SELECT * FROM progress_checkpoints WHERE student_id = ? ORDER BY created_at",
                studentId);
    }

    @PostMapping("/checkpoints")
    public String addCheckpoint(@RequestBody Map<String, Object> body) {
        jdbc.update("""
        INSERT INTO progress_checkpoints (tutor_id, student_id, subject_id, title)
        VALUES (?, ?, ?, ?)
        """,
                body.get("tutor_id"),
                body.get("student_id"),
                body.get("subject_id"),
                body.get("title")
        );
        return "{\"message\": \"Checkpoint added\"}";
    }

    @PatchMapping("/checkpoints/{id}")
    public String updateCheckpoint(@PathVariable int id, @RequestBody Map<String, Object> body) {
        boolean completed = Boolean.parseBoolean(body.get("completed").toString());
        jdbc.update(
                "UPDATE progress_checkpoints SET completed = ?, completed_at = ? WHERE id = ?",
                completed, completed ? java.time.LocalDateTime.now() : null, id);
        return "{\"message\": \"Checkpoint updated\"}";
    }

    @PatchMapping("/tutors/{id}/avatar")
    public String updateAvatar(@PathVariable int id, @RequestBody Map<String, Object> body) {
            jdbc.update(
                    "UPDATE tutors SET avatar_style = ? WHERE id = ?",
                    body.get("avatar_style"), id
            );
            return "{\"message\": \"Avatar updated\"}";
    }
    // ── Parents ──────────────────────────────────────────────────
    @PostMapping("/parents/magic-link")
    public org.springframework.http.ResponseEntity<?> requestMagicLink(@RequestBody Map<String, Object> body) {
        String email = body.get("email").toString();
        String token = java.util.UUID.randomUUID().toString();
        java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusHours(1);
        try {
            List<Map<String, Object>> existing = jdbc.queryForList(
                    "SELECT id FROM parents WHERE email = ?", email);
            if (existing.isEmpty()) {
                return org.springframework.http.ResponseEntity.status(404)
                        .body(Map.of("error", "No parent account found with that email."));
            }
            jdbc.update("UPDATE parents SET magic_link_token = ?, magic_link_expiry = ? WHERE email = ?",
                    token, expiry, email);
            return org.springframework.http.ResponseEntity.ok(
                    Map.of("message", "Magic link sent", "token", token));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/parents/login/{token}")
    public org.springframework.http.ResponseEntity<?> magicLinkLogin(@PathVariable String token) {
        try {
            Map<String, Object> parent = jdbc.queryForMap(
                    "SELECT * FROM parents WHERE magic_link_token = ? AND magic_link_expiry > NOW()", token);
            jdbc.update("UPDATE parents SET magic_link_token = NULL, magic_link_expiry = NULL WHERE id = ?",
                    parent.get("id"));
            parent.remove("magic_link_token");
            return org.springframework.http.ResponseEntity.ok(parent);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid or expired link."));
        }
    }

    @GetMapping("/parents/{id}/students")
    public List<Map<String, Object>> getParentStudents(@PathVariable int id) {
        return jdbc.queryForList(
                "SELECT * FROM students WHERE parent_id = ?", id);
    }

    // ── Session notes ─────────────────────────────────────────────
    @PostMapping("/session-notes")
    public org.springframework.http.ResponseEntity<?> addSessionNote(@RequestBody Map<String, Object> body) {
        jdbc.update("""
        INSERT INTO session_notes (booking_id, tutor_id, student_id, session_date, notes, homework, progress_summary)
        VALUES (?, ?, ?, NOW(), ?, ?, ?)
        """,
                body.get("booking_id"),
                body.get("tutor_id"),
                body.get("student_id"),
                body.get("notes"),
                body.get("homework"),
                body.get("progress_summary")
        );
        jdbc.update("UPDATE metrics SET value = value + 1, updated_at = NOW() WHERE metric = 'bookings_made'");
        return org.springframework.http.ResponseEntity.ok(Map.of("message", "Session note saved"));
    }

    @GetMapping("/session-notes/student/{studentId}")
    public List<Map<String, Object>> getStudentNotes(@PathVariable int studentId) {
        return jdbc.queryForList(
                "SELECT sn.*, t.first_name as tutor_first, t.last_name as tutor_last FROM session_notes sn JOIN tutors t ON t.id = sn.tutor_id WHERE sn.student_id = ? ORDER BY sn.session_date DESC",
                studentId);
    }

    @GetMapping("/session-notes/tutor/{tutorId}")
    public List<Map<String, Object>> getTutorNotes(@PathVariable int tutorId) {
        return jdbc.queryForList(
                "SELECT sn.*, s.first_name as student_first, s.last_name as student_last FROM session_notes sn JOIN students s ON s.id = sn.student_id WHERE sn.tutor_id = ? ORDER BY sn.session_date DESC",
                tutorId);
    }

    // ── Session checklist ─────────────────────────────────────────
    @PostMapping("/checklist")
    public org.springframework.http.ResponseEntity<?> addChecklist(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> objectives = (List<String>) body.get("objectives");
        for (String obj : objectives) {
            jdbc.update(
                    "INSERT INTO session_checklist (booking_id, tutor_id, student_id, objective) VALUES (?, ?, ?, ?)",
                    body.get("booking_id"), body.get("tutor_id"), body.get("student_id"), obj);
        }
        return org.springframework.http.ResponseEntity.ok(Map.of("message", "Checklist saved"));
    }

    @GetMapping("/checklist/{bookingId}")
    public List<Map<String, Object>> getChecklist(@PathVariable int bookingId) {
        return jdbc.queryForList(
                "SELECT * FROM session_checklist WHERE booking_id = ? ORDER BY created_at", bookingId);
    }

    @PatchMapping("/checklist/item/{id}")
    public String updateChecklistItem(@PathVariable int id, @RequestBody Map<String, Object> body) {
        jdbc.update("UPDATE session_checklist SET completed = ? WHERE id = ?",
                body.get("completed"), id);
        return "{\"message\": \"Updated\"}";
    }

    // ── Metrics ───────────────────────────────────────────────────
    @GetMapping("/metrics")
    public List<Map<String, Object>> getMetrics() {
        return jdbc.queryForList("SELECT * FROM metrics ORDER BY metric");
    }

    // ── Reviews ───────────────────────────────────────────────────
    @PostMapping("/reviews")
    public org.springframework.http.ResponseEntity<?> addReview(@RequestBody Map<String, Object> body) {
        jdbc.update("""
        INSERT INTO reviews (reviewer_id, reviewer_type, reviewee_id, reviewee_type, booking_id, rating, comment, is_private, attendance, engagement, preparedness)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
                body.get("reviewer_id"),
                body.get("reviewer_type"),
                body.get("reviewee_id"),
                body.get("reviewee_type"),
                body.get("booking_id"),
                body.get("rating"),
                body.get("comment"),
                body.getOrDefault("is_private", false),
                body.getOrDefault("attendance", null),
                body.getOrDefault("engagement", null),
                body.getOrDefault("preparedness", null)
        );
        if ("student".equals(body.get("reviewer_type"))) {
            jdbc.update("""
            UPDATE tutors SET
              rating = (SELECT AVG(rating) FROM reviews WHERE reviewee_id = ? AND reviewee_type = 'tutor' AND is_private = false),
              total_reviews = total_reviews + 1
            WHERE id = ?
            """, body.get("reviewee_id"), body.get("reviewee_id"));
            jdbc.update("UPDATE metrics SET value = value + 1, updated_at = NOW() WHERE metric = 'reviews_submitted'");
        }
        return org.springframework.http.ResponseEntity.ok(Map.of("message", "Review submitted"));
    }

    @GetMapping("/reviews/tutor/{tutorId}")
    public List<Map<String, Object>> getTutorReviews(@PathVariable int tutorId) {
        return jdbc.queryForList("""
                SELECT r.*, s.first_name as reviewer_name FROM reviews r
                LEFT JOIN students s ON s.id = r.reviewer_id
                WHERE r.reviewee_id = ? AND r.reviewee_type = 'tutor' AND r.is_private = false
                ORDER BY r.created_at DESC
                """, tutorId);
    }

        @GetMapping("/reviews/pending")
        public List<Map<String, Object>> getPendingReviews() {
            return jdbc.queryForList("""
        SELECT r.*, s.first_name as reviewer_name,
               t.first_name as reviewee_name
        FROM reviews r
        LEFT JOIN students s ON s.id = r.reviewer_id AND r.reviewer_type = 'student'
        LEFT JOIN tutors t ON t.id = r.reviewee_id AND r.reviewee_type = 'tutor'
        WHERE r.is_private = false AND r.comment IS NOT NULL
        ORDER BY r.created_at DESC
        """);
        }

        @PatchMapping("/reviews/{id}/approve")
        public String approveReview(@PathVariable int id) {
            jdbc.update("UPDATE reviews SET is_private = false WHERE id = ?", id);
            jdbc.update("UPDATE metrics SET value = value + 1, updated_at = NOW() WHERE metric = 'reviews_submitted'");
            return "{\"message\": \"Review approved\"}";
        }

        @PatchMapping("/reviews/{id}/reject")
        public String rejectReview(@PathVariable int id) {
            jdbc.update("DELETE FROM reviews WHERE id = ?", id);
            return "{\"message\": \"Review rejected\"}";
        }

    // ── Tutor approval ────────────────────────────────────────────
    @GetMapping("/admin/tutors/pending")
    public List<Map<String, Object>> getPendingTutors() {
        return jdbc.queryForList("""
        SELECT t.*, string_agg(s.name, ', ') as subject_names
        FROM tutors t
        LEFT JOIN tutor_subjects ts ON ts.tutor_id = t.id
        LEFT JOIN subjects s ON s.id = ts.subject_id
        WHERE t.status = 'pending'
        GROUP BY t.id
        ORDER BY t.submitted_at DESC
        """);
    }

    @GetMapping("/admin/tutors/all")
    public List<Map<String, Object>> getAllTutors() {
        return jdbc.queryForList("""
        SELECT t.*, string_agg(s.name, ', ') as subject_names
        FROM tutors t
        LEFT JOIN tutor_subjects ts ON ts.tutor_id = t.id
        LEFT JOIN subjects s ON s.id = ts.subject_id
        GROUP BY t.id
        ORDER BY t.status, t.created_at DESC
        """);
    }

    @PatchMapping("/admin/tutors/{id}/approve")
    public org.springframework.http.ResponseEntity<?> approveTutor(@PathVariable int id) {
        jdbc.update("UPDATE tutors SET status = 'approved' WHERE id = ?", id);
        jdbc.update("UPDATE metrics SET value = value + 1, updated_at = NOW() WHERE metric = 'tutors_onboarded'");
        return org.springframework.http.ResponseEntity.ok(Map.of("message", "Tutor approved"));
    }

    @PatchMapping("/admin/tutors/{id}/suspend")
    public org.springframework.http.ResponseEntity<?> suspendTutor(@PathVariable int id) {
        jdbc.update("UPDATE tutors SET status = 'suspended' WHERE id = ?", id);
        return org.springframework.http.ResponseEntity.ok(Map.of("message", "Tutor suspended"));
    }

    @PatchMapping("/admin/tutors/{id}/reject")
    public org.springframework.http.ResponseEntity<?> rejectTutor(@PathVariable int id) {
        jdbc.update("DELETE FROM tutors WHERE id = ? AND status = 'pending'", id);
        return org.springframework.http.ResponseEntity.ok(Map.of("message", "Tutor rejected"));
    }

    // ── Google Sheets webhook ─────────────────────────────────────
    @PostMapping("/webhook/tutor-signup")
    public org.springframework.http.ResponseEntity<?> tutorSignupWebhook(@RequestBody Map<String, Object> body) {
        try {
            String firstName = body.getOrDefault("first_name", "").toString();
            String lastName = body.getOrDefault("last_name", "").toString();
            String email = body.getOrDefault("email", "").toString();
            String phone = body.getOrDefault("phone", "").toString();
            String bio = body.getOrDefault("bio", "").toString();
            String philosophy = body.getOrDefault("teaching_philosophy", "").toString();
            String sessionTypes = body.getOrDefault("session_types", "both").toString();
            String zoomLink = body.getOrDefault("zoom_link", "").toString();
            String venmo = body.getOrDefault("venmo", "").toString();
            String cashapp = body.getOrDefault("cashapp", "").toString();
            String zelle = body.getOrDefault("zelle", "").toString();
            String keywordsRaw = body.getOrDefault("keywords", "").toString();
            double hourlyRate = 0;
            try { hourlyRate = Double.parseDouble(body.getOrDefault("hourly_rate", "0").toString()); } catch(Exception ignored) {}

            // check duplicate
            List<Map<String,Object>> existing = jdbc.queryForList("SELECT id FROM tutors WHERE email = ?", email);
            if (!existing.isEmpty()) {
                return org.springframework.http.ResponseEntity.status(409)
                        .body(Map.of("error", "Tutor with this email already exists"));
            }

            // insert tutor
            jdbc.update("""
            INSERT INTO tutors (first_name, last_name, email, phone, bio, hourly_rate,
                teaching_philosophy, session_types, zoom_link, venmo, cashapp, zelle,
                keywords, status, submitted_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,'pending',NOW())
            """,
                    firstName, lastName, email, phone, bio, hourlyRate,
                    philosophy, sessionTypes, zoomLink, venmo, cashapp, zelle, keywordsRaw
            );

            // insert subjects if provided
            String subjectsRaw = body.getOrDefault("subjects", "").toString();
            if (!subjectsRaw.isEmpty()) {
                Map<String, Object> newTutor = jdbc.queryForMap("SELECT id FROM tutors WHERE email = ?", email);
                int tutorId = ((Number) newTutor.get("id")).intValue();
                for (String subjectName : subjectsRaw.split(",")) {
                    String trimmed = subjectName.trim();
                    try {
                        Map<String, Object> subject = jdbc.queryForMap(
                                "SELECT id FROM subjects WHERE LOWER(name) = LOWER(?)", trimmed);
                        jdbc.update("INSERT INTO tutor_subjects (tutor_id, subject_id) VALUES (?,?) ON CONFLICT DO NOTHING",
                                tutorId, subject.get("id"));
                    } catch (Exception ignored) {}
                }
            }

            return org.springframework.http.ResponseEntity.ok(
                    Map.of("message", "Tutor application received — pending admin approval"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
