package com.ideal402.urban.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WithdrawUser {

    private String password;

    public WithdrawUser(String password) {
        this.password = password;
    }
}
