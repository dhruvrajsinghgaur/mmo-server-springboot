# MMO Game Server — Spring Boot

A production-grade real-time multiplayer game server built from scratch with Spring Boot, WebSocket, MySQL, and JWT authentication. Players connect, fight each other on a 100x100 grid, pick up weapons, and compete to be the last one standing.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 25 | Core language |
| Spring Boot 3.5 | Backend framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database ORM |
| Spring WebSocket | Real-time bidirectional communication |
| MySQL 8.4 | Persistent player storage |
| JWT (jjwt 0.12) | Stateless authentication tokens |
| BCrypt | Password hashing |
| Gson | JSON serialization |
| Lombok | Boilerplate reduction |
| Maven | Dependency management |

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
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/players` | Register new player | No |
| POST | `/login` | Login and get JWT token | No |

### Players
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/players` | Get all players | No |
| GET | `/players/{id}` | Get player by ID | No |
| PUT | `/players/{id}` | Update player stats | Yes |
| DELETE | `/players/{id}` | Delete player | Yes |

### Game
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/game/state` | Get current world state | No |

---

## WebSocket Protocol

Connect to: `ws://localhost:8080/game`

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

| Direction | dx | dy |
|---|---|---|
| UP | 0 | -1 |
| DOWN | 0 | 1 |
| LEFT | -1 | 0 |
| RIGHT | 1 | 0 |

---

## Setup

### Prerequisites
- Java 21+
- MySQL 8+
- Maven

### Database
```sql
CREATE DATABASE mmo;
ALTER TABLE players ADD UNIQUE (username);
```

### Configuration
Copy `application.properties.example` to `application.properties`:
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

## Quick Test

Open `game.html` in a browser — use WASD buttons to move, Attack to fight. Open two tabs with different usernames to play against yourself.

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

This is a Spring Boot rebuild of the original raw Java socket MMO:
[MMO Server Java](https://github.com/dhruvrajsinghgaur/MMO-Server-Java)

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
- Production backend patterns