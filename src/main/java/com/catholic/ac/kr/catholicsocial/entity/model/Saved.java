package com.catholic.ac.kr.catholicsocial.entity.model;

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
        uniqueConstraints = @UniqueConstraint(
                name = "uk_saved_user_moment", columnNames = {"user_id", "moment_id"}
        ),
        indexes = {
                @Index(name = "idx_saved_user", columnList = "user_id"),
                @Index(name = "idx_saved_moment", columnList = "moment_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Saved {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Moment moment;

    private LocalDateTime createdAt;

    @PrePersist
    protected void create() {
        this.createdAt = LocalDateTime.now();
    }
}
