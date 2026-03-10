package com.ideal402.urban;

import com.ideal402.urban.service.UserService;
import com.ideal402.urban.service.dto.WithdrawUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController{

    private final UserService userService;


    @PostMapping("/alarm/{regionId}")
    public ResponseEntity<Void> setAlarms(
            @PathVariable Integer regionId,
            @AuthenticationPrincipal String email
    ){
        userService.addAlarm(email, regionId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/alarm/{regionId}")
    public ResponseEntity<Void> deleteAlarms(
            @PathVariable Integer regionId,
            @AuthenticationPrincipal String email
    ){
        userService.deleteAlarm(email, regionId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> withdrawUser(
            @RequestBody @Valid WithdrawUser withdrawUser,
            @AuthenticationPrincipal String email,
            HttpServletRequest request
    ) {

        String password = withdrawUser.getPassword();

        userService.withdrawUser(email, password);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

}
