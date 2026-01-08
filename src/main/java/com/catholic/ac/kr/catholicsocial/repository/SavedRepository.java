package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.Saved;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.projection.SavedProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedRepository extends JpaRepository<Saved, Long> {
    @Query("""
            SELECT s.id AS id,
                   s.user.id AS userId,
                   s.moment.id AS momentId
            FROM Saved s
            where s.user.id = :userId
            """)
    Page<SavedProjection> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    boolean existsByUserAndMoment(User user, Moment moment);

    Optional<Saved> findByUser_IdAndMoment_Id(Long userId, Long momentId);

    @Query("""
                SELECT s.moment.id
                FROM Saved s
                WHERE s.user.id = :userId AND s.moment.id IN :momentIds
            """)
    List<Long> findSavedMomentIds(
            @Param("userId") Long userId,
            @Param("momentIds") List<Long> momentIds
    );

    @Query("""
            SELECT s.moment.id
            FROM Saved  s
            WHERE s.user.id = :userId AND s.moment.id IN :momentIds
            """)
    List<Long> findAllByMomentIds(@Param("userId") Long userId, @Param("momentIds") List<Long> momentIds);
}
