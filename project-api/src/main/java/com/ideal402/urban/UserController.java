package com.ideal402.urban;

import com.ideal402.urban.api.controller.UserApi;
import com.ideal402.urban.api.dto.WithdrawUserRequest;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;


    @Override
    public ResponseEntity<Void> setAlarms(Integer regionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = getCurrentUser();

        userService.addAlarm(user, regionId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> deleteAlarms(Integer regionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = getCurrentUser();

        userService.deleteAlarm(user, regionId);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> withdrawUser(WithdrawUserRequest  withdrawUserRequest) {

        User user = getCurrentUser();
        String password = withdrawUserRequest.getPassword();

        userService.withdrawUser(user, password);

        //현재 스레드의 인증정보 제거
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
