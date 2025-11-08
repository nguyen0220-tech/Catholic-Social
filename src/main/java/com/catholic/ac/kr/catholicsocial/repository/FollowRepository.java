package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.Follow;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.status.FollowState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {
    List<Follow> findByFollowerIdAndState(Long followerId, FollowState state, Pageable pageable);

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

}
