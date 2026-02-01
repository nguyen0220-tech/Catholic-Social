package com.catholic.ac.kr.catholicsocial.entity.model;

import com.catholic.ac.kr.catholicsocial.status.LogRoomContent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        indexes = @Index(columnList = "chat_room_id", name = "idx_chat_room_id")
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LogChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LogRoomContent content;

    @Column(nullable = false)
    private Long actorId;

    @Column(nullable = false)
    private LocalDateTime created;

    protected void create() {
        created = LocalDateTime.now();
    }
}
