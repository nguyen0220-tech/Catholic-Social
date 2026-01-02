package com.catholic.ac.kr.catholicsocial.security.systemservice;

import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.model.RefreshToken;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenUtil {
    private final RefreshTokenRepository refreshTokenRepository;

    public ApiResponse<RefreshToken> createRefreshToken(User user,String deviceId, String userAgent, String ipAddress) {
        List<RefreshToken> tokenList = refreshTokenRepository.findByUserAndDeviceId(user, deviceId);

        if (!tokenList.isEmpty()) {
            for (RefreshToken refreshToken : tokenList) {
                if (!refreshToken.isRevoked()){
                    refreshToken.setRevoked(true);
                }
            }
            refreshTokenRepository.saveAll(tokenList);
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setDeviceId(deviceId);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setRefreshToken(generateRefreshToken());

        refreshTokenRepository.save(refreshToken);

        return ApiResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(),
                "Refresh token created",refreshToken);
    }

    public String generateRefreshToken() {
        String refreshToken;
        do {
            refreshToken = UUID.randomUUID().toString();
        }while (refreshTokenRepository.findByRefreshToken(refreshToken).isPresent());

        return refreshToken;
    }

    public boolean isValid(RefreshToken token) {
        return token != null && !token.isRevoked() && token.getExpiryTime().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void clearRefreshTokensExpired() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteRefreshTokenExpired(now);

    }
}
