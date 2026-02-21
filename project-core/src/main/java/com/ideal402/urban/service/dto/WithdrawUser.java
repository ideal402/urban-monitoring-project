package com.ideal402.urban.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WithdrawUser {

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;

    public WithdrawUser(String password) {
        this.password = password;
    }
}
