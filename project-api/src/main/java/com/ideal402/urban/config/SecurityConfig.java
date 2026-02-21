package com.ideal402.urban.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signout").authenticated()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/map/**", "/error").permitAll()

                        .requestMatchers("/user/**").authenticated()

                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );


                return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // [중요] allowCredentials(true)일 경우 allowedOrigins에 "*"를 사용할 수 없습니다.
        // 프론트엔드 개발 서버의 정확한 Origin을 명시해야 합니다. (Vite 기본 포트인 경우 5173)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // 브라우저의 Preflight(사전 요청)를 통과하기 위해 OPTIONS 메서드가 반드시 포함되어야 합니다.
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));

        // 프론트엔드의 Axios withCredentials: true 와 짝을 이루는 필수 설정
        configuration.setAllowCredentials(true);

        // 세션 쿠키(JSESSIONID 등)를 헤더에 정상적으로 노출시키기 위한 설정
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 엔드포인트("/**")에 대해 위에서 정의한 configuration 정책을 적용합니다.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
