package com.example.grpc_chat_service.repository;

import com.example.grpc_chat_service.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    
    List<ChatMessageEntity> findTop100ByRoomIdOrderByTimestampDesc(String roomId);
}