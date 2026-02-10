package com.ideal402.urban;

import com.ideal402.urban.api.dto.WithdrawUserRequest;
import com.ideal402.urban.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController{

    private final UserService userService;


    @PostMapping("/alarm/{regionId}")
    public ResponseEntity<Void> setAlarms(
            @PathVariable Integer regionId,
            @SessionAttribute(name = "email", required = false) String email
    ){
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }

        userService.addAlarm(email, regionId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/alarm/{regionId}")
    public ResponseEntity<Void> deleteAlarms(
            @PathVariable Integer regionId,
            @SessionAttribute(name = "email", required = false) String email
    ){
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }

        userService.deleteAlarm(email, regionId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> withdrawUser(
            @RequestBody @Valid WithdrawUserRequest  withdrawUserRequest,
            @SessionAttribute(name = "email", required = false) String email,
            HttpServletRequest request
    ) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }

        userService.withdrawUser(email, withdrawUserRequest.getPassword());

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.noContent().build();
    }

}
