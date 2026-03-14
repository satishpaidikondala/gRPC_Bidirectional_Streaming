# gRPC Bidirectional Streaming Chat Service

A high-performance real-time chat application built with Spring Boot, gRPC, and PostgreSQL. Features include bidirectional streaming for instant messaging, room management, and persistent message history.

## 🚀 Key Features

- **Bidirectional Streaming**: Real-time message exchange between multiple clients and the server.
- **Microservices Architecture**: Containerized using Docker for easy deployment and scaling.
- **PostgreSQL Persistence**: All non-system messages are persisted for historical retrieval.
- **Dual API Support**: 
  - **REST**: Room management and message history.
  - **gRPC**: High-performance streaming and real-time notifications.
- **Dynamic State**: Real-time tracking of active users per room.

## 🛠️ Tech Stack

- **Java 21** / **Spring Boot 3**
- **gRPC** (Protobuf)
- **Spring Data JPA** (PostgreSQL)
- **Docker** & **Docker Compose**
- **Postman** (for API testing)

## 🏃 Running the Application

### 1. Prerequisites
- Docker and Docker Compose installed.

### 2. Startup
Clone the repository and run:
```bash
docker-compose up --build -d
```
The application will be available at:
- **REST API**: `http://localhost:8080`
- **gRPC Server**: `localhost:9090`

## 🧪 Testing and Verification

### Postman (Recommended)
We provide pre-configured collections for instant testing:
1. Import `docs/postman/rest_collection.json` for room management.
2. Import `docs/postman/grpc_collection.json` for real-time chat (ensure you import `src/main/proto/chat.proto` as the service definition).

### Command Line
A test script is provided for gRPC verification:
```bash
chmod +x chat-client.sh
./chat-client.sh
```

## 📂 Project Structure

- `src/main/proto/chat.proto`: Protobuf service and message definitions.
- `src/main/java/com/example/grpc_chat_service/service/ChatServiceImpl.java`: Core gRPC logic and broadcasting.
- `src/main/java/com/example/grpc_chat_service/controller/ChatController.java`: REST API implementation.
- `src/main/java/com/example/grpc_chat_service/entity/ChatMessageEntity.java`: JPA persistence model.
- `docs/API_TESTING.md`: Detailed instructions for Postman and manual testing.
- `docs/GRPC_SPEC.md`: Technical specification of all gRPC methods and models.
- `docs/REST_SPEC.md`: Technical specification of all REST endpoints and models.

## 📝 API Endpoints

### REST Endpoints
- `POST /api/rooms`: Create a new room.
- `GET /api/rooms`: List all active rooms with user counts.
- `GET /api/rooms/{roomId}/messages/history`: Retrieve last 100 messages.

### gRPC Methods
- `JoinRoom`: Server-streaming to join a room.
- `ChatStream`: Bidirectional streaming for real-time chat.
- `GetRoomUsers`: Unary RPC to list users in a room.
- `LeaveRoom`: Unary RPC to exit gracefully.