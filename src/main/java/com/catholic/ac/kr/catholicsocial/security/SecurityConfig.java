package com.catholic.ac.kr.catholicsocial.security;

import com.catholic.ac.kr.catholicsocial.custom.CustomAuthenticationEntryPoint;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.repository.UserRepository;
import com.catholic.ac.kr.catholicsocial.security.tokencommon.JwtAuthFilter;
import com.catholic.ac.kr.catholicsocial.security.userdetails.CustomUseDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserRepository userRepository;
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("user not found"));

            if (!user.isEnabled()) {
                throw new DisabledException("user is disabled");
            }
            return new CustomUseDetails(user);
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
        /*
        Spring chưa cung cấp constructor nhận cả UserDetailsService và PasswordEncoder
         */
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(
                                        "/ws/**",
                                        "/auth/**",
                                        "/user/find-username", "/user/find-password", "/user/reset-password", "/user/verify-reset-password",
                                        "/*.html", "/*.css", "/*.js",
                                        "/*.png", "/*.jpg", "/*.svg",
                                        "/icon/**", "/media/**").permitAll()
                                .requestMatchers("/graphiql", "/graphiql/**").permitAll()
                                .requestMatchers("/graphql").permitAll() // Cho phép gọi GraphQL (dev)
                                .anyRequest()
                                .authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }
}

    /*
    Khi người dùng truy cập /auth/login và nhập username/password,
Spring Security:

kiểm tra thông tin đăng nhập

nếu đúng, tạo HTTP Session (chứa thông tin người dùng)

gửi lại cookie JSESSIONID cho client.

Các request tiếp theo client gửi kèm cookie này → server dựa vào session để xác thực người dùng.

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf( csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll().anyRequest().authenticated())
                .formLogin(form ->
                        form.loginPage("/login")
                                .permitAll()
                )
                .logout(logout ->logout.permitAll());

        return http.build();
    }

     */
