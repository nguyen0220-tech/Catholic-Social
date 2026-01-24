package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("""
            SELECT r
            FROM ChatRoom r
            JOIN ChatRoomMember m1 ON m1.chatRoom = r
            JOIN ChatRoomMember m2 ON m2.chatRoom = r
            WHERE m1.user.id = :userId1 AND m2.user.id = :userId2
            """)
    Optional<ChatRoom> findExistingRoom(Long userId1, Long userId2);

}
