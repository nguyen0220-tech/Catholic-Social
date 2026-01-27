package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoomMember;
import com.catholic.ac.kr.catholicsocial.projection.ChatRoomProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    @Query("""
            SELECT cr.id AS chatRoomId
            FROM ChatRoomMember crm
            JOIN ChatRoom cr ON crm.chatRoom.id = cr.id
            WHERE crm.user.id = :userId
            ORDER BY cr.createdAt ASC
            """)
    Page<ChatRoomProjection> findByUserId(Long userId, Pageable pageable);

    boolean existsByUser_IdAndChatRoom_Id(Long userId, Long chatRoomId);

    @Query("""
                SELECT crm
                FROM ChatRoomMember crm
                JOIN FETCH crm.user u
                JOIN FETCH u.userInfo
                WHERE crm.chatRoom.id IN :chatRoomIds
            """)
    List<ChatRoomMember> findMembersByChatRoomIds(List<Long> chatRoomIds);

    @Query("""
            SELECT m.user.id
            FROM ChatRoomMember m
            WHERE m.chatRoom.id = :chatRoomId
            """)
    List<Long> findMemberIdsByChatRoomId(Long chatRoomId);
}
