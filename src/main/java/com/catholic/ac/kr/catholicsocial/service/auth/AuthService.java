package com.catholic.ac.kr.catholicsocial.service.auth;

import com.catholic.ac.kr.catholicsocial.custom.EntityUtils;
import com.catholic.ac.kr.catholicsocial.wrapper.ApiResponse;
import com.catholic.ac.kr.catholicsocial.entity.dto.TokenResponseDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.LoginRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.LogoutRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.RefreshTokenRequest;
import com.catholic.ac.kr.catholicsocial.entity.dto.request.SignUpRequest;
import com.catholic.ac.kr.catholicsocial.entity.model.*;
import com.catholic.ac.kr.catholicsocial.exception.ResourceNotFoundException;
import com.catholic.ac.kr.catholicsocial.repository.RefreshTokenRepository;
import com.catholic.ac.kr.catholicsocial.repository.RoleRepository;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.repository.VerificationTokenRepository;
import com.catholic.ac.kr.catholicsocial.security.systemservice.RefreshTokenUtil;
import com.catholic.ac.kr.catholicsocial.security.tokencommon.JwtUtil;
import com.catholic.ac.kr.catholicsocial.security.tokencommon.VerificationTokenService;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final Map<String, Integer> loginFailCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> loginFailTimes = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenUtil refreshTokenUtil;

    public ApiResponse<String> signUp(SignUpRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());

        if (user.isPresent()) {
            return ApiResponse.fail(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                    request.getUsername() + " is already in use");
        }

        boolean phoneExists = userRepository.existsUserByPhone(request.getPhone());

        if (phoneExists) {
            return ApiResponse.fail(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                    request.getPhone() + " already exists");
        }

        boolean emailExists = userRepository.existsUserByEmail(request.getEmail());

        if (emailExists) {
            return ApiResponse.fail(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                    request.getEmail() + " already exists");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRoles(Set.of(
                roleRepository.findByName("ROLE_USER").orElseThrow(
                        () -> new ResourceNotFoundException("role not exists"))));

        UserInfo info = new UserInfo();
        info.setFirstName(request.getFirstName());
        info.setLastName(request.getLastName());
        info.setEmail(request.getEmail());
        info.setPhone(request.getPhone());
        info.setBirthday(request.getBirthDate());
        info.setSex(request.getSex());
        newUser.setInfo(info);

        userRepository.save(newUser);

        verificationTokenService.sendVerificationToken(newUser);

        return ApiResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(),
                newUser.getUsername() + " sign up successful, please verify your email");
    }

    public boolean verifyUser(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElse(null);

        if (verificationToken == null || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        } else {
            User user = verificationToken.getUser();

            user.setEnabled(true);

            userRepository.save(user);

            verificationTokenRepository.delete(verificationToken);

            return true;
        }
    }

    public ApiResponse<TokenResponseDTO> login(
            LoginRequest request,
            HttpServletRequest httpServletRequest) {

        String deviceId = httpServletRequest.getHeader("deviceId");
        String userAgent = httpServletRequest.getHeader("user-agent");
        String ipAddress = httpServletRequest.getRemoteAddr();

        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            return ApiResponse.fail(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                    request.getUsername() + " Tài khoản không tồn tại");
        }

        User user = userOptional.get();

        Long lockTime = loginFailTimes.get(user.getUsername());

        if (lockTime != null) {
            if (System.currentTimeMillis() < lockTime) {
                long remainingTime = loginFailTimes.get(user.getUsername()) - System.currentTimeMillis();
                long remainingSeconds = remainingTime / 1000;
                return ApiResponse.fail(HttpStatus.LOCKED.value(), HttpStatus.LOCKED.getReasonPhrase(),
                        request.getUsername() + " bị khóa tạm thời, vui lòng thử lại sau " + remainingSeconds + "s");
            } else {
                loginFailTimes.remove(user.getUsername());
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            CustomUseDetails useDetails = (CustomUseDetails) authentication.getPrincipal();

            Map<String, Object> claims = new HashMap<>();

            claims.put("id", user.getId());
            claims.put("username", user.getUsername());
            claims.put("roles", user.getRoles().stream().map(Role::getName).toList());

            String accessToken = jwtUtil.generateAccessToken(useDetails.getUsername(), claims);

            RefreshToken refreshToken = refreshTokenUtil.createRefreshToken(
                            user,
                            deviceId,
                            userAgent,
                            ipAddress)
                    .getData();

            TokenResponseDTO tokenResponseDTO = new TokenResponseDTO();

            tokenResponseDTO.setUserId(user.getId());
            tokenResponseDTO.setAccessToken(accessToken);
            tokenResponseDTO.setRefreshToken(refreshToken.getRefreshToken());


            loginFailCounts.remove(request.getUsername());
            loginFailTimes.remove(user.getUsername());

            return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                    "Login success", tokenResponseDTO);

        } catch (Exception e) {
            if (e instanceof DisabledException || (e.getCause() instanceof DisabledException)) {
                return ApiResponse.fail(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                        request.getUsername() + " chưa kích hoạt, vui lòng xác nhận qua email");

            } else if (e instanceof BadCredentialsException || (e.getCause() instanceof BadCredentialsException)) {
                int count = loginFailCounts.getOrDefault(request.getUsername(), 0) + 1;
                loginFailCounts.put(request.getUsername(), count);

                if (count >= 5) {
                    long lockUntil = System.currentTimeMillis() + Duration.ofMinutes(1).toMillis(); //block 1min
                    loginFailTimes.put(request.getUsername(), lockUntil);
                    loginFailCounts.remove(request.getUsername());

                    long remainingMillis = lockUntil - System.currentTimeMillis();
                    long remainingSeconds = remainingMillis / 1000;

                    return ApiResponse.fail(HttpStatus.LOCKED.value(), HttpStatus.LOCKED.getReasonPhrase(),
                            request.getUsername() + " bị khóa tạm thời, vui lòng thử lại sau " + remainingSeconds + "s");
                }
                return ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "Mật khẩu đăng nhập sai " + count + "/5 lần");
            } else
                return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Login failed");
        }
    }

    public ApiResponse<String> logout(LogoutRequest request) {
        RefreshToken token = EntityUtils.getOrThrow(refreshTokenRepository.findByRefreshToken(request.getRefreshToken()),"Token ");

        if (!refreshTokenUtil.isValid(token)) {
            return ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Login failed");
        }

        refreshTokenUtil.revoke(token);

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), "Logout success");

    }

    public ApiResponse<TokenResponseDTO> refreshToken(RefreshTokenRequest request) {
        RefreshToken token = EntityUtils.getOrThrow(refreshTokenRepository.findByRefreshToken(request.getRefreshToken()), "Token ");

        if (!refreshTokenUtil.isValid(token)) {
            return ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Refresh token expired");
        }

        refreshTokenUtil.revoke(token);

        User user = token.getUser();

        Map<String, Object> claims = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "roles", user.getRoles().stream().map(Role::getName).toList()
        );

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), claims);

        RefreshToken newRefreshToken = refreshTokenUtil.createRefreshToken(
                        user,
                        token.getDeviceId(),
                        token.getUserAgent(),
                        token.getIpAddress())
                .getData();

        TokenResponseDTO tokenResponseDTO = new TokenResponseDTO();
        tokenResponseDTO.setAccessToken(accessToken);
        tokenResponseDTO.setRefreshToken(newRefreshToken.getRefreshToken());
        tokenResponseDTO.setUserId(user.getId());

        return ApiResponse.success(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "Refresh token success", tokenResponseDTO);

    }
}
