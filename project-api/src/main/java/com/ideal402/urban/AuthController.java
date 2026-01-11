package com.ideal402.urban;

import com.ideal402.urban.api.controller.AuthApi;
import com.ideal402.urban.api.dto.AuthResponse;
import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponse> signup(SignupRequest request) throws Exception {

        AuthResponse response = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Override
    public ResponseEntity<AuthResponse> signin(SigninRequest request) throws Exception {

        AuthResponse response = authService.signin(request);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> signout() throws Exception {
        authService.signout();

        return ResponseEntity.ok().build();
    }
}
