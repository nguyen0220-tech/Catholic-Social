package com.catholic.ac.kr.catholicsocial.entity.model;

import com.catholic.ac.kr.catholicsocial.status.NotifyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_noti_user", columnList = "user_id"),
                @Index(name = "idx_noti_user_entity", columnList = "user_id, entity_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    private Long entityId; //FOLLOW, SYSTEM... => null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotifyType type;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void created() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
}
