package com.example.grpc_chat_service.controller;

import com.example.grpc_chat_service.entity.ChatMessageEntity;
import com.example.grpc_chat_service.repository.ChatMessageRepository;
import com.example.grpc_chat_service.service.ChatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/rooms")
public class ChatController {

    @Autowired
    private ChatMessageRepository repository;

    @Autowired
    private ChatServiceImpl chatService;

    private final List<Map<String, Object>> rooms = new ArrayList<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createRoom(@RequestBody Map<String, String> body) {
        Map<String, Object> room = new HashMap<>();
        room.put("roomId", UUID.randomUUID().toString());
        room.put("roomName", body.get("roomName"));
        room.put("description", body.get("description"));
        rooms.add(room);
        return room;
    }

    @GetMapping
    public List<Map<String, Object>> listRooms() {
        // We add the user count dynamically based on the active gRPC connections
        for (Map<String, Object> room : rooms) {
            String roomId = (String) room.get("roomId");
            // This is where we bridge the REST controller with the Service data
            room.put("userCount", chatService.getActiveUserCount(roomId)); 
        }
        return rooms;
    }

    @GetMapping("/{roomId}/messages/history")
    public List<ChatMessageEntity> getHistory(@PathVariable String roomId) {
        return repository.findTop100ByRoomIdOrderByTimestampDesc(roomId);
    }
}