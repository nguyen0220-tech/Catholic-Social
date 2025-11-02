package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.entity.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    @Query("""
            SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
            FROM VerificationToken t
            WHERE t.user = :user
            """)
    boolean existsByUser(@Param("user") User user);
}
