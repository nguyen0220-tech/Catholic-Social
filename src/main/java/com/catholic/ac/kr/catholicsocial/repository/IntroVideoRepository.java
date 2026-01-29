package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.dto.IntroVideoDTO;
import com.catholic.ac.kr.catholicsocial.entity.model.IntroVideo;
import com.catholic.ac.kr.catholicsocial.projection.IntroProjection;
import com.catholic.ac.kr.catholicsocial.status.IntroStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IntroVideoRepository extends JpaRepository<IntroVideo, Long> {

    @Query("""
            SELECT new com.catholic.ac.kr.catholicsocial.entity.dto.IntroVideoDTO(it.id, it.url, it.exp)
            FROM IntroVideo it
            WHERE it.user.id = :userId AND it.status = :status
            """)
    Optional<IntroVideoDTO> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") IntroStatus status);

    Optional<IntroVideo> findByUser_IdAndStatus(Long userId, IntroStatus status);

    Optional<IntroVideo> findByIdAndUser_Id(Long id, Long userId);

    @Query("""
            SELECT it
            FROM IntroVideo it
            WHERE it.exp <= :now
            """)
    List<IntroVideo> findIntroVideoExpired(@Param("now") LocalDateTime now);

    @Query("""
            SELECT it.id AS id
            FROM IntroVideo it
            WHERE it.user.id = :userId AND it.exp > :now AND it.status = :status
            """)
    Page<IntroProjection> findAllIntroCanRestore(@Param("userId") Long userId, @Param("now") LocalDateTime now,
                                                 @Param("status") IntroStatus status, Pageable pageable);

}
