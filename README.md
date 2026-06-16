# MMO Game Server — Spring Boot

A production-grade real-time multiplayer game server built from scratch with Spring Boot, WebSocket, MySQL, and JWT authentication. Players connect, fight each other on a 100x100 grid, pick up weapons, and compete to be the last one standing.

**🎮 Live demo:** [mmo-server-springboot-production.up.railway.app](https://mmo-server-springboot-production.up.railway.app) — containerized with Docker and deployed on Railway. Click the link, enter a username, and play directly in your browser.

---

## Screenshots

| Login | Live multiplayer session (2 players) |
| --- | --- |
| ![Login screen](screenshots/login.png) | ![Two players in the arena](screenshots/gameplay.png) |

---

## Tech Stack

| Technology       | Purpose                               |
| ---------------- | -------------------------------------- |
| Java 25          | Core language                          |
| Spring Boot 3.5  | Backend framework                      |
| Spring Security  | Authentication and authorization       |
| Spring Data JPA  | Database ORM                           |
| Spring WebSocket | Real-time bidirectional communication  |
| MySQL 8.4        | Persistent player storage              |
| JWT (jjwt 0.12)  | Stateless authentication tokens        |
| BCrypt           | Password hashing                       |
| Gson             | JSON serialization                     |
| Lombok           | Boilerplate reduction                  |
| Maven            | Dependency management                  |
| Docker           | Containerization (multi-stage build)   |
| Docker Compose   | Local multi-container orchestration    |
| Railway          | Cloud deployment                       |

---

## Architecture

```
HTTP Request / WebSocket Message
            ↓
     JwtFilter (validates token)
            ↓
     SecurityConfig (allow/block)
            ↓
     Controller / WebSocketHandler
            ↓
     GameService (game logic)
            ↓
     GameState (in-memory world)
            ↓
     PlayerRepository (MySQL)

GameLoop (runs every 50ms)
     → broadcasts world state to all players
     → checks for winner
```

---

## Project Structure

```
com.mmo.mmo_server
├── Auth
│   ├── JWTServices.java         JWT token generation and validation
│   ├── JwtFilter.java           Intercepts every request to validate token
│   └── SecurityConfig.java      Spring Security configuration
│
├── Player (Database)
│   ├── Players.java             JPA entity — persistent player data
│   ├── PlayerRepository.java    Database queries
│   └── PlayerController.java    REST API endpoints
│
├── Game (In-Memory)
│   ├── GamePlayer.java          Player state during a game session
│   ├── GameState.java           Singleton world state
│   ├── GameService.java         Game logic — movement, combat, pickup
│   ├── GameLoop.java            50ms tick — broadcasts state to all players
│   └── GameWebSocket.java       WebSocket handler — JOIN, MOVE, ATTACK, PING
│
├── Items
│   ├── Items.java               Abstract base class for all items
│   └── Weapon.java              Weapon with attack power, range, durability
│
├── Enums
│   ├── Action.java              IDLE, MOVING, ATTACKING, DEAD, RESPAWNING
│   └── GamePhase.java           WAITING, PLAYING, ENDED
│
└── Config
    └── WebSocketConfig.java     Registers WebSocket endpoint at /game
```

---

## Game Features

- 100x100 grid world
- Players spawn at random positions
- WASD movement with grid bounds validation
- Directional combat — attack in the direction you face
- Melee and ranged weapons with different range values
- Armor degradation system — weapons wear down enemy armor per hit
- Weapon pickup — walk over a weapon to equip it
- Cooldown system — prevents attack and movement spam
- Death and respawn system
- Disconnect handling — ghost players removed automatically
- Game phase system — WAITING → PLAYING → ENDED
- Winner detection — last player alive wins
- World resets after each game
- State broadcast every 50ms to all connected players

---

## REST API

### Auth

| Method | Endpoint   | Description             | Auth |
| ------ | ---------- | ------------------------ | ---- |
| POST   | `/players` | Register new player      | No   |
| POST   | `/login`   | Login and get JWT token  | No   |

### Players

| Method | Endpoint        | Description           | Auth |
| ------ | --------------- | ----------------------- | ---- |
| GET    | `/players`      | Get all players         | No   |
| GET    | `/players/{id}` | Get player by ID        | No   |
| PUT    | `/players/{id}` | Update player stats     | Yes  |
| DELETE | `/players/{id}` | Delete player           | Yes  |

### Game

| Method | Endpoint      | Description              | Auth |
| ------ | ------------- | -------------------------- | ---- |
| GET    | `/game/state` | Get current world state    | No   |

---

## WebSocket Protocol

Connect to: `ws://localhost:8080/game` (local) or `wss://mmo-server-springboot-production.up.railway.app/game` (live)

### Client → Server

```json
{ "type": "JOIN",   "username": "dhruv" }
{ "type": "MOVE",   "username": "dhruv", "dx": 1, "dy": 0 }
{ "type": "ATTACK", "username": "dhruv", "dx": 0, "dy": -1 }
{ "type": "PING" }
```

### Server → Client

```json
{
  "type": "STATE",
  "players": [
    { "username": "dhruv", "x": 10, "y": 15, "hp": 100, "action": "IDLE" },
    { "username": "john",  "x": 20, "y": 30, "hp": 75,  "action": "MOVING" }
  ],
  "weapons": 6
}

{ "type": "WINNER", "winner": "dhruv" }
{ "type": "PONG" }
```

---

## Direction Vectors

| Direction | dx  | dy  |
| --------- | --- | --- |
| UP        | 0   | -1  |
| DOWN      | 0   | 1   |
| LEFT      | -1  | 0   |
| RIGHT     | 1   | 0   |

---

## Running the Project

### Option A: Docker Compose (recommended)

**Prerequisites:** Docker + Docker Compose

```bash
git clone https://github.com/dhruvrajsinghgaur/mmo-server-springboot.git
cd mmo-server-springboot
docker compose up -d --build
```

This builds the app inside a multi-stage Docker image (Maven build stage → lightweight JRE runtime stage), starts MySQL in its own container with a persistent volume so data survives restarts, waits for MySQL's healthcheck before starting the app, and connects both containers over a private Docker network — no local Java or MySQL installation required.

Server starts on `http://localhost:8080`.

```bash
docker compose logs -f mmo-server   # watch startup logs
docker compose down                 # stop everything
```

### Option B: Run locally without Docker

**Prerequisites:** Java 21+, MySQL 8+, Maven

```sql
CREATE DATABASE mmo;
```

Copy `src/main/resources/application.properties.example` to `application.properties` and fill in your local MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mmo
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```

```bash
mvn spring-boot:run
```

Server starts on `http://localhost:8080`.

---

## Deployment

The app is containerized with a multi-stage Dockerfile — one stage compiles the jar with Maven, the second runs it on a minimal JRE image — keeping the final image small and free of build tooling. It's deployed to [Railway](https://railway.app), with configuration (database URL, JWT secret, port) injected entirely through environment variables. The `${VAR:default}` pattern used throughout `application.properties` means the exact same jar runs correctly whether it's started locally, via Docker Compose, or on Railway — only the environment changes.

The deployment has been validated with a real multiplayer session: two players connecting over the public internet, moving around the grid, picking up weapons, and fighting in real time.

---

## Quick Test

Click the live demo link above — it now serves the game directly, with the server field auto-filled to match wherever the page is hosted, so no manual setup is needed. To test a local instance instead, run the server (see below) and visit `http://localhost:8080`. Open two tabs (or share the link with a friend) with different usernames to fight.

---

## Planned Features

- Save match results to database after game ends
- XP and leveling from kills
- Armor class with durability
- Weapon drops on death
- Separate attack and movement cooldowns
- Spawn protection
- Lobby system with minimum player count
- Heartbeat ping with auto-disconnect on timeout

---

## Related Project

This is a Spring Boot rebuild of the original raw Java socket MMO: [MMO Server Java](https://github.com/dhruvrajsinghgaur/MMO-Server-Java)

The original covers raw TCP sockets, multithreading, room-based navigation, and turn-based combat — built without any frameworks.

---

## Learning Goals

This project was built to understand:

- Spring Boot REST API design
- JWT stateless authentication and filter chains
- Real-time WebSocket communication
- In-memory game state management across concurrent threads
- Authoritative server model — server validates all game actions
- Game loop architecture and state broadcasting
- Docker containerization with multi-stage builds
- Container orchestration with Docker Compose (custom networks, healthchecks, persistent volumes)
- Cloud deployment and environment-based configuration
- Production backend patterns