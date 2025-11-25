package com.catholic.ac.kr.catholicsocial.config;

import com.catholic.ac.kr.catholicsocial.interceptor.RateLimitUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final RateLimitUtils rateLimitUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitUtils)
                .addPathPatterns("/auth/**")
                .excludePathPatterns();
    }

}
