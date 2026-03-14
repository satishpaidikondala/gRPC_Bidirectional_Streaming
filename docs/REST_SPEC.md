# REST API Specification

This document provides the technical details for interacting with the **Chat Service** REST API.

## 🌐 Base URL
- **Server Address**: `http://localhost:8080/api`
- **Protocol**: HTTP/1.1
- **Format**: JSON

---

## 🛠️ Endpoints

### 1. Create a Room
*Initialize a new chat room with a custom name and description.*

- **Method**: `POST`
- **Path**: `/rooms`
- **Status Codes**: 
  - `201 Created`: Room created successfully.
  - `400 Bad Request`: Validation failure.

**Request Body:**
```json
{
    "roomName": "General",
    "description": "General Discussion Room"
}
```

**Response Body:**
```json
{
    "roomId": "ea9349dd-f441-4715-a68b-457e3758775a",
    "roomName": "General",
    "description": "General Discussion Room",
    "userCount": 0
}
```

---

### 2. List Rooms
*Retrieve all available chat rooms along with their live user counts.*

- **Method**: `GET`
- **Path**: `/rooms`
- **Status Codes**: 
  - `200 OK`: Success.

**Sample Response:**
```json
[
    {
        "roomId": "ea9349dd-f441-4715-a68b-457e3758775a",
        "roomName": "General",
        "userCount": 5
    }
]
```

---

### 3. Get Room History
*Retrieve the last 100 messages for a specific room.*

- **Method**: `GET`
- **Path**: `/rooms/{roomId}/messages/history`
- **Status Codes**: 
  - `200 OK`: Success.
  - `404 Not Found`: Room does not exist.

**Response format**: List of messages sorted by timestamp (newest first).

**Sample Response:**
```json
[
    {
        "messageId": "66936b76-7676-438d-b90f-8cbc851ec46e",
        "roomId": "ea9349dd-f441-4715-a68b-457e3758775a",
        "userId": "user-1",
        "username": "Alice",
        "content": "Hello world",
        "type": "TEXT",
        "timestamp": "2026-03-14T05:30:12.409Z"
    }
]
```

---

## 📦 Persistence Rules
- Messages are saved to the `chat_messages` table in PostgreSQL.
- Only messages of type `TEXT` and `IMAGE_URL` are persisted.
- System messages (`SYSTEM_JOIN`, `SYSTEM_LEAVE`) are handled in memory for real-time broadcast and are **not** persisted to the history.
