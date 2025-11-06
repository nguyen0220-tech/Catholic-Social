package com.catholic.ac.kr.catholicsocial.entity.model;

import com.catholic.ac.kr.catholicsocial.status.MomentShare;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Moment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String content;

    @OneToMany(mappedBy = "moment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @Enumerated(EnumType.STRING)
    private MomentShare share;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void create() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void setListImage(List<Image> images) {
        this.images = images;
        for (Image image : images) {
            image.setMoment(this);
        }
    }

    @PreUpdate
    protected void update() {
        this.updatedAt = LocalDateTime.now();
    }
}
