package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Heart;
import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.projection.HeartProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeartRepository extends JpaRepository<Heart,Long> {
    @Query("""
            SELECT h.id AS id,
                   h.user.id AS userId,
                   h.moment.id AS momentId
                        FROM Heart h
                        WHERE h.moment.id = :momentId
            """)
    List<HeartProjection> findByMomentId(@Param("momentId") Long momentId, Pageable pageable);

    boolean existsByUserAndMoment(User user, Moment moment);

    Optional<Heart> findByUserAndMoment(User user, Moment moment);

    List<Heart> findAllByMoment_IdIn(List<Long> momentIds);

    boolean existsByUser_IdAndMoment_Id(Long userId, Long momentId);
}
