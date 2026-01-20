package com.catholic.ac.kr.catholicsocial.entity.model;

import com.catholic.ac.kr.catholicsocial.status.IntroStatus;
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
        indexes = {
                @Index(columnList = "user_id, status", name = "idx_user_status"),
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IntroVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false) //delete video in cloudinary
    private String publicId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime exp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntroStatus status;

    @PrePersist
    protected void create() {
        this.createdAt = LocalDateTime.now();
        this.status = IntroStatus.ACTIVE;
    }
}
