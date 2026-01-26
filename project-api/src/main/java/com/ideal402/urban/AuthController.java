package com.ideal402.urban;

import com.ideal402.urban.api.controller.AuthApi;
import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final HttpServletRequest httpRequest;

    @Override
    public ResponseEntity<Void> signup(SignupRequest request) throws Exception {

        authService.signup(request, httpRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build(); // Body 없음
    }

    @Override
    public ResponseEntity<Void> signin(SigninRequest request) throws Exception {

        authService.signin(request, httpRequest);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> signout() throws Exception {

        authService.signout(httpRequest);

        return ResponseEntity.ok().build();
    }
}
