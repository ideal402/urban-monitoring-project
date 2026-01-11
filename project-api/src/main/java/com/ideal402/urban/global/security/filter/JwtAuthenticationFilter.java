package com.ideal402.urban.global.security.filter;

import com.ideal402.urban.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Request Header에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 유효성 검사
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 3. Redis에 해당 토큰이 로그아웃(Blacklist)으로 등록되어 있는지 확인
            // (key: token, value: "logout")
            String isLogout = redisTemplate.opsForValue().get(token);

            if (ObjectUtils.isEmpty(isLogout)) {
                // 4. 토큰이 유효하고 로그아웃되지 않았다면 인증 객체 생성
                Authentication authentication = getAuthentication(token);

                // 5. SecurityContextHolder에 저장 (이 요청이 끝날 때까지 인증된 상태로 유지됨)
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다", authentication.getName());
            } else {
                log.warn("로그아웃된 토큰으로 접근을 시도했습니다.");
            }
        }

        // 6. 다음 필터로 넘김 (통과)
        filterChain.doFilter(request, response);
    }

    // 헤더에서 Bearer 토큰 꺼내기
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 문자열만 반환
        }
        return null;
    }

    // 토큰에서 정보 추출하여 인증 객체(Authentication) 생성
    private Authentication getAuthentication(String token) {
        // 토큰에서 이메일 추출
        String email = jwtTokenProvider.getEmail(token);

        // 현재는 권한(Role)이 없으므로 USER 권한을 강제로 부여 (추후 DB에서 가져오게 확장 가능)
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // UserDetails 객체 생성 (비밀번호는 보안상 빈 문자열)
        UserDetails principal = new User(email, "", authorities);

        // UsernamePasswordAuthenticationToken 생성하여 반환
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}