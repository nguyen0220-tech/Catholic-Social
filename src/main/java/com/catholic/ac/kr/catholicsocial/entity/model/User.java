package com.catholic.ac.kr.catholicsocial.entity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Table(name = "users")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;

    private boolean looked;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role", // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "user_id"),// FK user
            inverseJoinColumns = @JoinColumn(name = "role_id")// FK role
    )
    private Set<Role> roles;

    /*
    cascade = CascadeType.ALL -> khi lưu User, JPA sẽ tự động lưu UserInfo
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserInfo userInfo;

    @PrePersist
    protected void create() {
        this.looked = false;
    }

    public void setInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        userInfo.setUser(this);
    }
}
