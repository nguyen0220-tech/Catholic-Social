package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
