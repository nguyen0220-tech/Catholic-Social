package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Message;
import com.catholic.ac.kr.catholicsocial.projection.MessageProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
            SELECT m.id AS id,
                   m.sender.id AS senderId,
                   m.text AS text,
                   m.createdAt AS createdAt
            FROM Message m
            WHERE m.chatRoom.id = :chatRoomId
            """)
    Page<MessageProjection> findByChatRoomId(Long chatRoomId, Pageable pageable);

    @Query("""
                SELECT crm.user.id
                FROM Message m
                JOIN ChatRoomMember crm ON crm.chatRoom.id = m.chatRoom.id
                WHERE m.sender.id = :userId
                  AND crm.user.id <> :userId
                  AND crm.chatRoom.type = 'ONE_TO_ONE'
                GROUP BY crm.user.id
                ORDER BY MAX(m.createdAt) DESC
            """)
    List<Long> findRecentUsers(Long userId, Pageable pageable);

}
