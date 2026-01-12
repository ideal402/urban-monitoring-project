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
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;


    public ResponseEntity<Void> setAlarms(Integer regionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        userService.save(user, regionId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> deleteAlarms(Integer regionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        userService.delete(user, regionId);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> withdrawUser(WithdrawUserRequest  withdrawUserRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        String password = withdrawUserRequest.getPassword();

        userService.withdrawUser(user, password);

        return ResponseEntity.noContent().build();
    }
}
