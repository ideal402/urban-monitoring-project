package com.ideal402.urban;

import com.ideal402.urban.api.controller.UserApi;
import com.ideal402.urban.api.dto.WithdrawUserRequest;
import com.ideal402.urban.common.AuthenticationFailedException;
import com.ideal402.urban.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;


    private final HttpServletRequest httpRequest;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername(); // 이메일 반환
        }
        throw new AuthenticationFailedException("로그인 정보가 유효하지 않습니다.");
    }

    @Override
    public ResponseEntity<Void> setAlarms(@PathVariable Integer regionId) {
        String email = getCurrentUserEmail();

        userService.addAlarm(email, regionId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> deleteAlarms(Integer regionId) {
        String email = getCurrentUserEmail();

        userService.deleteAlarm(email, regionId);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> withdrawUser(WithdrawUserRequest  withdrawUserRequest) {

        String email = getCurrentUserEmail();
        String password = withdrawUserRequest.getPassword();

        userService.withdrawUser(email, password, httpRequest);

        return ResponseEntity.noContent().build();
    }

}
