package com.catholic.ac.kr.catholicsocial.security.tokencommon;

import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUserDetailsService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

//    @Autowired
    private final CustomUserDetailsService customUserDetailsService;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, Map<String, Object> claims) {
        return generateToken(username, claims, Duration.ofHours(5));
    }

    public String generateToken(String username, Map<String, Object> claims, Duration duration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + duration.toMillis()))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
    public Authentication getAuthentication(String token) {
        String username = extractUsername(token);

        CustomUseDetails useDetails = (CustomUseDetails) customUserDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(username, null, useDetails.getAuthorities());
    }

     */

}
