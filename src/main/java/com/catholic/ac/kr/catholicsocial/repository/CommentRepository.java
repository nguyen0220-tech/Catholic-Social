package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Comment;
import com.catholic.ac.kr.catholicsocial.projection.CommentProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository  extends JpaRepository<Comment, Long> {
    @Query("""
            SELECT     c.id AS id,
                       c.comment AS comment,
                       c.createdAt AS createdAt,
                       c.moment.id AS momentId,
                       c.user.id AS userId
            FROM Comment c
            WHERE c.moment.id = :momentId
            """)
    List<CommentProjection> findByMomentId(@Param("momentId") Long momentId, Pageable pageable);
}
