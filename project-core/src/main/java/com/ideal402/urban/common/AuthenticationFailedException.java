package com.ideal402.urban.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 인증 실패 (비밀번호 불일치 등) 시 발생하는 예외
 * HTTP Status: 401 Unauthorized
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}