package com.catholic.ac.kr.catholicsocial.entity.model;

import com.catholic.ac.kr.catholicsocial.status.ActiveType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_active_user", columnList = "user_id"), //tên cột trong DB
                @Index(name = "idx_active_type_entity", columnList = "type, entityId"),
                @Index(name = "idx_active_created", columnList = "createdAt")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Active {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long entityId; //sử dụng để FK đến các entity (Moment, Heart, Comment)
    // - thay vì liên kết trục tiếp tới mỗi entity như User

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    private ActiveType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void create() {
        this.createdAt = LocalDateTime.now();
    }
}
