package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.dto.MemberOfChatRoomDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.ChatRoomMember;
import com.catholic.ac.kr.catholicsocial.projection.ChatRoomProjection;
import com.catholic.ac.kr.catholicsocial.status.ChatRoomMemberStatus;
import com.catholic.ac.kr.catholicsocial.status.ChatRoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    @Query("""
            SELECT cr.id AS chatRoomId
            FROM ChatRoomMember crm
            JOIN ChatRoom cr ON crm.chatRoom.id = cr.id
            WHERE crm.user.id = :userId AND crm.status = :status
            ORDER BY cr.lastMessageAt DESC
            """)
    Page<ChatRoomProjection> findByUserId(Long userId, ChatRoomMemberStatus status, Pageable pageable);

    boolean existsByUser_IdAndChatRoom_IdAndStatus(Long userId, Long chatRoomId, ChatRoomMemberStatus status);
    @Query("""
                SELECT crm
                FROM ChatRoomMember crm
                JOIN FETCH crm.user u
                JOIN FETCH u.userInfo
                WHERE crm.chatRoom.id IN :chatRoomIds
            """)
    List<ChatRoomMember> findMembersByChatRoomIds(List<Long> chatRoomIds);

    @Query("""
                SELECT new com.catholic.ac.kr.catholicsocial.entity.dto.MemberOfChatRoomDTO(
                            crm.user.id, crm.createdAt)
                FROM ChatRoomMember crm
                WHERE crm.chatRoom.id = :chatRoomId AND crm.status = :status
            """)
    Page<MemberOfChatRoomDTO> findMembersByChatRoomId(Long chatRoomId, ChatRoomMemberStatus status, Pageable pageable);

    @Query("""
            SELECT m.user.id
            FROM ChatRoomMember m
            WHERE m.chatRoom.id = :chatRoomId AND m.status = :status
            """)
    List<Long> findMemberIdsByChatRoomId(Long chatRoomId,ChatRoomMemberStatus status);

    Optional<ChatRoomMember> findByUser_IdAndChatRoom_Id(Long userId, Long chatRoomId);

    @Query("""
            SELECT m1.user.id, m1.chatRoom
            FROM ChatRoomMember m1
            JOIN ChatRoomMember m2 ON m1.chatRoom.id = m2.chatRoom.id
            WHERE m1.user.id IN :recipientIds AND m2.user.id = :currentUserId AND m2.chatRoom.type = :type
            """)
    List<Object[]> findAllByRecipientIdsAndUserId(
            List<Long> recipientIds,
            Long currentUserId,
            ChatRoomType type);
}
