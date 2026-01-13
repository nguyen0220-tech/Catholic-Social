package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Follow;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.projection.FollowerProjection;
import com.catholic.ac.kr.catholicsocial.status.FollowState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {
    List<Follow> findByFollowerIdAndState(Long followerId, FollowState state, Pageable pageable);

    List<Follow> findAllByFollowerAndState(User follower, FollowState state);

    boolean existsByFollowerAndUserAndState(User follower, User user, FollowState state);

    @Query("""
            SELECT f FROM Follow f
            WHERE f.follower = :follower AND f.user = :user AND f.state NOT IN ('BLOCKED','FOLLOWING')
            """)
    Optional<Follow> findByFollowerAndUser(User follower, User user);

    @Query("""
            SELECT f FROM Follow f
            WHERE f.follower = :follower AND f.user = :user
            """)
    Optional<Follow> findByFollowerAndUser_Action(User follower, User user);

    List<Follow> findByUserIdAndState(Long userId, FollowState state, Pageable pageable);

    int countFollowByUserIdAndState(Long userId, FollowState state);

    int countFollowByFollowerIdAndState(Long followerId, FollowState state);

    @Query("""
            SELECT f.user.id FROM Follow f WHERE f.follower.id = :userId AND f.state = 'BLOCKED'
            UNION
            SELECT f.follower.id FROM Follow f WHERE f.user.id = :userId AND f.state = 'BLOCKED'
            """)
    List<Long> findUserIdsBlocked(Long userId);

    @Query("""
            SELECT f.user.id
            FROM Follow f
            WHERE f.follower.id = :followerId AND f.user.id IN :userIds AND f.state = 'FOLLOWING'
            """)
    List<Long> findUserIdsFollowing(Long followerId, List<Long> userIds);

    @Query("""
            SELECT f2.follower.id AS userId
            FROM Follow f1
            JOIN Follow f2 ON f1.user.id = f2.follower.id
            WHERE f1.follower.id = :meId
              AND f1.state = 'FOLLOWING'
              AND f2.user.id = :profileId
              AND f2.state = 'FOLLOWING'
            """)
    List<FollowerProjection> findMutualFollowers(Long profileId, Long meId, Pageable pageable);


    @Query("""
                SELECT f.follower.id AS userId
                FROM Follow f
                WHERE f.user.id = :userId
                  AND f.state = 'FOLLOWING'
                  AND NOT EXISTS (
                      SELECT 1 FROM Follow b
                      WHERE b.state = 'BLOCKED'
                        AND (
                            (b.user.id = :viewerId AND b.follower.id = f.follower.id)
                         OR (b.user.id = f.follower.id AND b.follower.id = :viewerId)
                        )
                  )
            """)
    Page<FollowerProjection> findFollowersByUserId(
            @Param("userId") Long userId,
            @Param("viewerId") Long viewerId,
            Pageable pageable
    );

    @Query("""
            SELECT f.user.id AS userId
            FROM Follow f
            WHERE f.follower.id = :userId
              AND f.state = 'FOLLOWING'
              AND NOT EXISTS (
                  SELECT 1 FROM Follow b
                  WHERE b.state = 'BLOCKED'
                    AND (
                        (b.user.id = :viewerId AND b.follower.id = f.user.id)
                     OR (b.user.id = f.user.id AND b.follower.id = :viewerId)
                    )
              )
            """)
    Page<FollowerProjection> findFollowingByUserId(
            @Param("userId") Long userId,
            @Param("viewerId") Long viewerId,
            Pageable pageable);
}
