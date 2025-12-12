package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Moment;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {

    @Query("""
            SELECT m FROM Moment m
                     WHERE (m.share = 'PUBLIC'
                           OR m.user.id = :userId
                           OR m.user.id IN (SELECT f.user.id FROM Follow f WHERE f.follower.id = :userId AND f.state = 'FOLLOWING'))
                           AND m.user.id NOT IN (SELECT f.user.id  FROM Follow f WHERE f.follower.id = :userId AND f.state ='BLOCKED'
                                                 UNION
                                                 SELECT f.follower.id  FROM Follow f WHERE f.user.id = :userId AND f.state ='BLOCKED' )
            """)
    List<Moment> findAllMomentFollowAndPublic(@Param("userId") Long userId, Pageable pageable);

    List<Moment> findByUserId(Long userId, Pageable pageable);

    Optional<Moment> findByIdAndUser(Long id, User user);
}
