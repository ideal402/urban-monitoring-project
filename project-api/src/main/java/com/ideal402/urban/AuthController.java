package com.ideal402.urban;

import com.ideal402.urban.api.controller.AuthApi;
import com.ideal402.urban.api.dto.SigninRequest;
import com.ideal402.urban.api.dto.SignupRequest;
import com.ideal402.urban.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<Void> signup(SignupRequest request) throws Exception {

        authService.signup(request);

        SigninRequest signinRequest = new SigninRequest(request.getEmail(), request.getPassword());
        authService.signin(signinRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> signin(@RequestBody @Valid SigninRequest request) throws Exception {

        authService.signin(request);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> signout() throws Exception {

        authService.signout();

        return ResponseEntity.ok().build();
    }
}
