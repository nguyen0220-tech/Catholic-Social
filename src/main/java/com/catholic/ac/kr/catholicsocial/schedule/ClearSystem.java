package com.catholic.ac.kr.catholicsocial.schedule;

import com.catholic.ac.kr.catholicsocial.security.systemservice.RefreshTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClearSystem {

    private final RefreshTokenUtil refreshTokenUtil;

    @Scheduled(cron = "0 0 3 * * * ")
    public void clearRefreshTokens() {
        refreshTokenUtil.clearRefreshTokensExpired();
    }
}
