package com.catholic.ac.kr.catholicsocial.entity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Table(name = "heart",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","moment_id"}))
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Heart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
                foreignKey = @ForeignKey(name = "fk_heart_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id",
                foreignKey = @ForeignKey(name = "fk_heart_moment"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Moment moment;

}
