# MMO Game Server — Spring Boot

A production-grade multiplayer game server built with Spring Boot, MySQL, and JWT authentication. This is a rebuild of an earlier raw Java socket MMO server, now using industry-standard backend technologies.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 25 | Core language |
| Spring Boot 3.5 | Backend framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database ORM |
| MySQL 8.4 | Persistent storage |
| JWT (jjwt 0.12) | Stateless authentication tokens |
| BCrypt | Password hashing |
| Lombok | Boilerplate reduction |
| Maven | Dependency management |

---

## Architecture

```
HTTP Request
      ↓
JwtFilter (validates token on every request)
      ↓
Controller (handles routing)
      ↓
Service (business logic)
      ↓
Repository (database queries)
      ↓
MySQL Database
```

---

## Current Features

- Player registration with BCrypt password hashing
- Unique username enforcement at database level
- JWT token generation on login — 24 hour expiry
- Full CRUD API for player management
- Spring Security filter chain configuration
- MySQL persistence with Hibernate auto schema update
- Proper HTTP status codes — 200, 401, 404, 409

---

## API Endpoints

### Auth
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/players` | Register new player | No |
| POST | `/login` | Login and receive JWT token | No |

### Players
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/players` | Get all players | No |
| GET | `/players/{id}` | Get player by ID | No |
| PUT | `/players/{id}` | Update player stats | No |
| DELETE | `/players/{id}` | Delete player | No |

> Auth protection coming in next update via JWT filter

---

## Request Examples

### Register
```json
POST /players
{
    "username": "dhruv",
    "password": "yourpassword",
    "hp": 100,
    "level": 1,
    "gold": 500
}
```

### Login
```json
POST /login
{
    "username": "dhruv",
    "password": "yourpassword"
}
```

### Login Response
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "dhruv",
    "id": 7
}
```

---

## Setup

### Prerequisites
- Java 21+
- MySQL 8+
- Maven

### Database Setup
```sql
CREATE DATABASE mmo;
```

### Configuration
Copy `application.properties.example` to `application.properties` and fill in your details:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mmo
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```

### Run
```bash
mvn spring-boot:run
```

Server starts on `http://localhost:8080`

---

## Planned Features

- JWT filter to protect game endpoints
- WebSocket support for real-time multiplayer
- Game state management
- Player inventory and weapons system
- Combat endpoints
- Lobby system
- Heartbeat and disconnect handling

---

## Related Project

This server is a continuation of the original raw Java socket MMO server:
[MMO Server Java](https://github.com/dhruvrajsinghgaur/MMO-Server-Java)

That project covers:
- Raw TCP socket programming
- Multithreading with CopyOnWriteArrayList
- Room-based world navigation
- Turn-based PvP combat
- Singleton design pattern

---

## Learning Goals

This project is being built to understand:
- Industry-standard REST API design
- JWT stateless authentication
- Spring Security filter chains
- JPA and database relationships
- Real-time WebSocket communication
- Production backend architecture
