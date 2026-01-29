package com.catholic.ac.kr.catholicsocial.entity.model;

import com.catholic.ac.kr.catholicsocial.status.ChatRoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;

    private String description;

    @Column(nullable = false)
    private LocalDateTime lastMessageAt;

    @Column(nullable = false)
    private String lastMessagePreview;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;

    @PrePersist
    protected void create() {
        this.createdAt = LocalDateTime.now();
    }
}
