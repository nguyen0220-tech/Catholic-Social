package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Notification;
import com.catholic.ac.kr.catholicsocial.projection.NotificationProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
            SELECT n.id AS id,
                   n.user.id AS userId,
                   n.actor.id AS actorId,
                   n.entityId AS entityId,
                   n.type AS type,
                   n.isRead AS isRead,
                   n.createdAt AS createdAt
            FROM Notification n
            WHERE n.user.id = :userId
            """)
    Page<NotificationProjection> findByNotificationsByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT COUNT(n)
            FROM Notification n
            WHERE n.user.id = :userId AND n.isRead = :read
            """)
    int countByUserIdAndRead(Long userId, boolean read);
}
