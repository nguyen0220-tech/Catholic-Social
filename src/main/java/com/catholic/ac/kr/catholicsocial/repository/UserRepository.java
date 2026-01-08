package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.dto.UserFollowDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.UserSuggestions;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.projection.UserProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM User u
            WHERE u.userInfo.phone = :phone
            """)
    boolean existsUserByPhone(@Param("phone") String phone);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM User u
            WHERE u.userInfo.email = :email
            """)
    boolean existsUserByEmail(@Param("email") String email);

    @Query("""
            SELECT u FROM User u
            WHERE u.userInfo.firstName = :firstName
                        AND u.userInfo.lastName = :lastName
                        AND u.userInfo.phone = :phone
            """)
    Optional<User> findByUserInfo_FirstNameAndLastNameAndPhone(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("phone") String phone);

    @Query("""
            SELECT u FROM User u
            WHERE u.username = :username
                        AND u.userInfo.email = :email
                        AND u.userInfo.phone = :phone
            """)
    Optional<User> findByUserInfo_UsernameAndEmailAndPhone(
            @Param("username") String username,
            @Param("email") String email,
            @Param("phone") String phone);

    @Query("""
            SELECT new com.catholic.ac.kr.catholicsocial.entity.dto.UserFollowDTO(
                        u.id,u.userInfo.firstName,u.userInfo.lastName,u.userInfo.avatarUrl)
                        FROM User u
                        WHERE u.id != :userId
                                    AND CONCAT(u.userInfo.firstName,' ',u.userInfo.lastName) LIKE LOWER( CONCAT('%', :keyword,'%'))
                                    AND NOT EXISTS (
                                                    SELECT 1
                                                    FROM Follow f
                                                    WHERE f.state = 'BLOCKED' AND (( f.follower.id = :userId AND f.user.id = u.id)
                                                                                    OR (f.user.id = :userId AND f.follower.id = u.id))
                                                    )
            """)
    List<UserFollowDTO> findUserFollowByUserId(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT u.id AS id  FROM User u WHERE u.id = :userId
            """)
    Optional<UserProjection> findUserProjectionById(@Param("userId") Long userId);

    @Query("""
            SELECT new com.catholic.ac.kr.catholicsocial.entity.dto.UserSuggestions(
            u.id, u.userInfo.firstName, u.userInfo.lastName, u.userInfo.avatarUrl, f1.user.userInfo.firstName, f1.user.userInfo.lastName )
            FROM Follow f1
            JOIN Follow f2 ON f1.user.id = f2.follower.id
            JOIN User u ON u.id = f2.user.id
            WHERE f1.follower.id = :userId
                  AND f1.state = 'FOLLOWING'
                  AND f2.state = 'FOLLOWING'
                  AND u.id != :userId
                  AND u.id NOT IN (
                              SELECT f.user.id FROM Follow f
                                       WHERE f.follower.id = :userId AND f.state = 'FOLLOWING'
                                   )
            
            """)
    List<UserSuggestions> findUserSuggestionsByUserId(Long userId);
}
