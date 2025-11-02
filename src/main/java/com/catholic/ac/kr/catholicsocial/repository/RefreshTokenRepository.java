package com.catholic.ac.kr.catholicsocial.repository;

import com.catholic.ac.kr.catholicsocial.entity.model.RefreshToken;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    List<RefreshToken> findByUserAndDeviceId(User user, String deviceId);

    @Modifying
    @Query("""
            DELETE FROM RefreshToken t WHERE t.expiryTime <= :now
            """)
    void deleteRefreshTokenExpired(@Param("now") LocalDateTime now);
}
