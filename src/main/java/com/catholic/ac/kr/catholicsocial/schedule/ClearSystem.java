package com.catholic.ac.kr.catholicsocial.schedule;

import com.catholic.ac.kr.catholicsocial.security.systemservice.RefreshTokenUtil;
import com.catholic.ac.kr.catholicsocial.service.IntroVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClearSystem {

    private final RefreshTokenUtil refreshTokenUtil;
    private final IntroVideoService introVideoService;

    @Scheduled(cron = "0 21 1 * * * ")
    public void clearRefreshTokens() {
        refreshTokenUtil.clearRefreshTokensExpired();
    }

    @Scheduled(cron = "0 30 1 * * * ")
    public void clearIntroVideos(){
        introVideoService.clearIntroExpired();
    }
}
