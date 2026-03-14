package com.example.grpc_chat_service.service;

import com.example.grpc.chat.*;
import com.example.grpc.chat.ChatMessage;
import com.example.grpc.chat.User;
import com.example.grpc.chat.MessageType;
import com.example.grpc.chat.ChatServiceGrpc;
import com.example.grpc.chat.JoinRoomRequest;
import com.example.grpc.chat.RoomRequest;
import com.example.grpc.chat.UserListResponse;
import com.example.grpc.chat.LeaveRoomRequest;
import com.example.grpc.chat.LeaveRoomResponse;
import com.example.grpc_chat_service.entity.ChatMessageEntity;
import com.example.grpc_chat_service.repository.ChatMessageRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    @Autowired
    private ChatMessageRepository repository;

    // To broadcast messages to all connected clients in a room
    private final Map<String, Set<StreamObserver<ChatMessage>>> roomObservers = new ConcurrentHashMap<>();
    
    // To track active users in a room (Requirement 8)
    private final Map<String, Set<User>> roomUsers = new ConcurrentHashMap<>();

    // Requirement 6: Join a room and receive a stream of messages
    @Override
    public void joinRoom(JoinRoomRequest request, StreamObserver<ChatMessage> responseObserver) {
        String roomId = request.getRoomId();
        String userId = request.getUserId();
        String username = request.getUsername();

        // 1. Register the observer for broadcasting
        roomObservers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(responseObserver);

        // 2. Track the user
        User user = User.newBuilder().setUserId(userId).setUsername(username).build();
        roomUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(user);

        // 3. Send a SYSTEM_JOIN message
        ChatMessage joinMsg = ChatMessage.newBuilder()
                .setRoomId(roomId)
                .setUserId(userId)
                .setUsername(username)
                .setType(MessageType.SYSTEM_JOIN)
                .setContent(username + " joined the room.")
                .setTimestamp(System.currentTimeMillis())
                .build();
        
        broadcastMessage(roomId, joinMsg);
    }

    // Requirement 7: Bidirectional streaming for chat messages
    @Override
    public StreamObserver<ChatMessage> chatStream(StreamObserver<ChatMessage> responseObserver) {
        return new StreamObserver<ChatMessage>() {
            @Override
            public void onNext(ChatMessage message) {
                String roomId = message.getRoomId();
                
                // Ensure the responseObserver is registered if not already (for bidirectional logic)
                roomObservers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(responseObserver);

                // Enrich message with Server-side ID and Timestamp if not present
                ChatMessage enrichedMsg = ChatMessage.newBuilder(message)
                        .setMessageId(UUID.randomUUID().toString())
                        .setTimestamp(System.currentTimeMillis())
                        .build();

                // Save to DB only if it's a real user message (Requirement 7 & 10)
                if (enrichedMsg.getType() == MessageType.TEXT || enrichedMsg.getType() == MessageType.IMAGE_URL) {
                    saveMessageToDb(enrichedMsg);
                }

                broadcastMessage(roomId, enrichedMsg);
            }

            @Override
            public void onError(Throwable t) {
                removeObserver(responseObserver);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                removeObserver(responseObserver);
            }
        };
    }

    // Requirement 8: List users in a room
    @Override
    public void getRoomUsers(RoomRequest request, StreamObserver<UserListResponse> responseObserver) {
        String roomId = request.getRoomId();
        Set<User> users = roomUsers.getOrDefault(roomId, Collections.emptySet());
        
        UserListResponse response = UserListResponse.newBuilder()
                .addAllUsers(users)
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Requirement 9: Leave a room and notify others
    @Override
    public void leaveRoom(LeaveRoomRequest request, StreamObserver<LeaveRoomResponse> responseObserver) {
        String roomId = request.getRoomId();
        String userId = request.getUserId();

        // 1. Remove user from tracking
        Set<User> users = roomUsers.get(roomId);
        if (users != null) {
            users.removeIf(u -> u.getUserId().equals(userId));
        }

        // 2. Broadcast SYSTEM_LEAVE message (Requirement 9)
        ChatMessage leaveMsg = ChatMessage.newBuilder()
                .setRoomId(roomId)
                .setUserId(userId)
                .setType(MessageType.SYSTEM_LEAVE)
                .setContent("User " + userId + " has left.")
                .setTimestamp(System.currentTimeMillis())
                .build();
        broadcastMessage(roomId, leaveMsg);

        responseObserver.onNext(LeaveRoomResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    private void broadcastMessage(String roomId, ChatMessage message) {
        Set<StreamObserver<ChatMessage>> observers = roomObservers.get(roomId);
        if (observers != null) {
            Iterator<StreamObserver<ChatMessage>> it = observers.iterator();
            while (it.hasNext()) {
                StreamObserver<ChatMessage> observer = it.next();
                try {
                    observer.onNext(message);
                } catch (Exception e) {
                    it.remove(); // Remove failed observers
                }
            }
        }
    }

    private void saveMessageToDb(ChatMessage msg) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setMessageId(UUID.randomUUID().toString());
        entity.setRoomId(msg.getRoomId());
        entity.setUserId(msg.getUserId());
        entity.setUsername(msg.getUsername());
        entity.setContent(msg.getContent());
        entity.setType(msg.getType().name());
        entity.setTimestamp(LocalDateTime.now());
        repository.save(entity);
    }

    private void removeObserver(StreamObserver<ChatMessage> observer) {
        roomObservers.values().forEach(set -> set.remove(observer));
    }

    // Bridge for REST Controller (listRooms user count)
    public int getActiveUserCount(String roomId) {
        Set<User> users = roomUsers.get(roomId);
        return users != null ? users.size() : 0;
    }
}