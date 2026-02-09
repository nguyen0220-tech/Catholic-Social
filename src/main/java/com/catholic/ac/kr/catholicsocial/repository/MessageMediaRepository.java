package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.MessageMedia;
import com.catholic.ac.kr.catholicsocial.projection.MessageMediaProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageMediaRepository extends JpaRepository<MessageMedia, Long> {
    @Query("""
            SELECT mm
            FROM MessageMedia mm
            WHERE mm.message.id IN :messageIds
            """)
    List<MessageMedia> findAllByMessageIds(List<Long> messageIds);

    @Query("""
            SELECT mm.id AS id, mm.url AS url, mm.user.id AS senderId
            FROM MessageMedia mm
            JOIN Message m ON mm.message.id = m.id
            WHERE m.chatRoom.id = :chatRoomId
            """)
    Page<MessageMediaProjection> findAllByChatRoomId(Long chatRoomId, Pageable pageable);
}
