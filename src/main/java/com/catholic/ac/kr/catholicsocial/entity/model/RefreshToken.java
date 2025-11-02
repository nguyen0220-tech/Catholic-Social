package com.catholic.ac.kr.catholicsocial.entity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String refreshToken;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private  boolean revoked;


    private String deviceId;

    private String userAgent;

    private String ipAddress;

    @PrePersist
    protected void create() {
        this.createTime = LocalDateTime.now();
        this.expiryTime = LocalDateTime.now().plusDays(3);
        this.revoked = false;
    }
}
