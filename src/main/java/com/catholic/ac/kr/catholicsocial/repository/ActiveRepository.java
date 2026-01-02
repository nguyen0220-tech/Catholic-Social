package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Active;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.projection.ActiveProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveRepository extends JpaRepository<Active, Long> {
    @Query("""
        SELECT a.id AS id,
               a.entityId AS entityId,
               a.type AS type,
               a.user.id AS userId
        FROM Active a
        WHERE a.user.id = :userId
""")
    Page<ActiveProjection> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
