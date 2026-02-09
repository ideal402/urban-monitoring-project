package com.ideal402.urban;

import com.ideal402.urban.api.controller.AuthApi;
import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController{

    private final AuthService authService;


    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest request, HttpServletRequest httpServletRequest) throws Exception {

        authService.signup(request);

        SigninRequest signinRequest = new SigninRequest(request.getEmail(), request.getPassword());
        authService.signin(signinRequest);

        CreateSession(httpServletRequest, request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/signin")
    public ResponseEntity<Void> signin(@RequestBody @Valid SigninRequest request, HttpServletRequest httpServletRequest) throws Exception {

        authService.signin(request);

        CreateSession(httpServletRequest, request.getEmail());

        return ResponseEntity.ok().build();
    }


    @PostMapping("/signout")
    public ResponseEntity<Void> signout(HttpServletRequest httpServletRequest) throws Exception {

        HttpSession session = httpServletRequest.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok().build();
    }

    public void CreateSession(HttpServletRequest httpServletRequest, String email) {
        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute("LOGIN_MEMBER", email);
        session.setMaxInactiveInterval(1800);
    }
}
