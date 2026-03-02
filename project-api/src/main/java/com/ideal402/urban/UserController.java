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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController{

    private final UserService userService;

    private final Executor asyncExecutor;

    @PostMapping("/alarm/{regionId}")
    public CompletableFuture<ResponseEntity<Void>> setAlarms(
            @PathVariable Integer regionId,
            @AuthenticationPrincipal String email
    ) {
        return CompletableFuture.runAsync(() -> {
            userService.addAlarm(email, regionId);
        }, asyncExecutor).thenApply(v -> {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        });
    }

    @DeleteMapping("/alarm/{regionId}")
    public CompletableFuture<ResponseEntity<Void>> deleteAlarms(
            @PathVariable Integer regionId,
            @AuthenticationPrincipal String email
    ) {
        return CompletableFuture.runAsync(() -> {
            userService.deleteAlarm(email, regionId);
        } ,asyncExecutor).thenApply(v -> {
            return ResponseEntity.noContent().build();
        });
    }

    @PostMapping("/delete")
    public CompletableFuture<ResponseEntity<Void>> withdrawUser(
            @RequestBody @Valid WithdrawUser withdrawUser,
            @AuthenticationPrincipal String email,
            HttpServletRequest request
    ) {
        return CompletableFuture.runAsync(() -> {
            String password = withdrawUser.getPassword();
            userService.withdrawUser(email, password);

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();
        }, asyncExecutor).thenApply(v -> {
            return ResponseEntity.noContent().build();
        });
    }

}
