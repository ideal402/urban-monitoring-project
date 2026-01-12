package com.ideal402.urban.service;

import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.entity.UserAlarm;
import com.ideal402.urban.domain.repository.UserAlarmRepository;
import com.ideal402.urban.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void addAlarm(User user, Integer regionId) {
        user.addAlarm(regionId);

        userRepository.save(user);
    }

    @Transactional
    public void deleteAlarm(User user, Integer regionId) {
        user.removeAlarm(regionId);

        userRepository.save(user);
    }


    @Transactional
    public void withdrawUser(User principal, String inputPassword) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));

        if (!passwordEncoder.matches(inputPassword, user.getPasswordHash())) {
            throw new org.springframework.security.access.AccessDeniedException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }
}

