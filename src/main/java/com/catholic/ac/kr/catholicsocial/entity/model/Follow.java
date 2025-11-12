package com.catholic.ac.kr.catholicsocial.entity.model;

import com.catholic.ac.kr.catholicsocial.status.FollowState;
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
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"follower_id", "user_id"})
        })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User follower; //Người theo dõi (user đang đăng nhập)

    @ManyToOne
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user; //Người được theo dõi

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowState state;

    @Column(nullable = false)
    private LocalDateTime followedAt;

    @PrePersist
    protected void create() {
        this.followedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void update() {
        this.followedAt = LocalDateTime.now();
    }

}
