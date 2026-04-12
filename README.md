# Tutwme 📚

A full-stack tutoring booking platform built for the **Elk Grove and Sacramento community** — because finding a local tutor shouldn't still happen through Facebook groups.

---

## 🌐 Live Demo
> Coming soon — deployment in progress on Railway

## 💻 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Database | PostgreSQL |
| Auth | BCrypt password hashing |
| Frontend | HTML, CSS, JavaScript |
| Deployment | Railway |
| Version Control | GitHub |

---

## ✨ Features

- **Booking System** — Students can browse and book tutoring sessions
- **Admin Dashboard** — Manage users, sessions, and platform activity
- **Community Chatbot** — Support and guidance for local users
- **Invite Code System** — Exclusive access for Elk Grove and Sacramento residents
- **Secure Authentication** — BCrypt hashed passwords

---

## 🗄️ Database Schema

The platform is backed by a PostgreSQL database with 5 core tables:

- `users` — students, tutors, and admins
- `tutors` — tutor profiles, subjects, and rates
- `bookings` — session details and status
- `availability` — tutor scheduling
- `invite_codes` — region-based access control

---

## 🚀 Running Locally

```bash
# 1. Clone the repo
git clone https://github.com/yourusername/tutwme.git

# 2. Set up PostgreSQL and create the database
psql -U postgres -c "CREATE DATABASE tutwme;"

# 3. Run the schema
psql -U postgres -d tutwme -f schema.sql

# 4. Configure environment variables
# Create an application.properties or .env file with:
# DB_URL, DB_USERNAME, DB_PASSWORD

# 5. Run the Spring Boot app
./mvnw spring-boot:run
```

---

## 🔧 Environment Variables

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |

---

## 📍 About

Elk Grove is one of California's fastest-growing cities. Despite that growth, connecting students with local tutors still largely happens informally. Tutwme was built to change that — a dedicated platform for the community, by the community.

---

## 👩‍💻 Author

**Erika** — Full-Stack Developer
[LinkedIn](https://linkedin.com/in/yourprofile) · [GitHub](https://github.com/yourusername)