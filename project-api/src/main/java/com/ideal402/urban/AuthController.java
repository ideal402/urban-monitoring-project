package com.ideal402.urban;

import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController{

    private final AuthService authService;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest request,
                                       HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse ) throws Exception {

        authService.signup(request);

        SigninRequest signinRequest = new SigninRequest(request.getEmail(), request.getPassword());
        authService.signin(signinRequest);

        saveAuthenticationData(request.getEmail(), httpRequest, httpResponse);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/signin")
    public ResponseEntity<Void> signin(@RequestBody @Valid SigninRequest request,
                                       HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse ) throws Exception {

        authService.signin(request);

        saveAuthenticationData(request.getEmail(), httpRequest, httpResponse);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/signout")
    public ResponseEntity<Void> signout(HttpServletRequest httpServletRequest) throws Exception {

        HttpSession session = httpServletRequest.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }


    private void saveAuthenticationData(String email, HttpServletRequest request, HttpServletResponse response) {

        // 1. Authentication 토큰 생성: Principal(주체) 영역에 email 할당
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

        // 2. 빈 SecurityContext 생성 및 Authentication 객체 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 3. SecurityContextRepository를 통해 SecurityContext를 HttpSession에 영속화(Persistence)
        securityContextRepository.saveContext(context, request, response);
    }
}
