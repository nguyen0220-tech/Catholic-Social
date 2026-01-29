package com.catholic.ac.kr.catholicsocial.entity.model;

import com.catholic.ac.kr.catholicsocial.status.ChatRoomMemberStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "user_id"}),
        indexes = @Index(columnList = "user_id, chat_room_id, status"))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatRoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    private ChatRoomMemberStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    protected void create(){
        this.status = ChatRoomMemberStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }
}
