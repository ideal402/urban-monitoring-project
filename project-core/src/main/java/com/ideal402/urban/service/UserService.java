package com.ideal402.urban.service;

import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.entity.UserAlarm;
import com.ideal402.urban.domain.repository.UserAlarmRepository;
import com.ideal402.urban.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Integer regionNum = 20;

    @Transactional
    public void addAlarm(String email, Integer regionId) {

        if (regionId == null || regionId < 0 || regionId > regionNum) {
            throw new IllegalArgumentException("Region Id can't be greater than 20");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isDuplicate = user.getAlarms().stream()
                .anyMatch(alarm -> alarm.getRegionId().equals(regionId));

        if (!isDuplicate) {
            user.addAlarm(regionId);
            userRepository.save(user);
        }
    }

    @Transactional
    public void deleteAlarm(String email, Integer regionId) {

        if (regionId == null || regionId < 0 || regionId > regionNum) {
            throw new IllegalArgumentException("Region Id can't be greater than 20");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isExist = user.getAlarms().stream()
                .anyMatch(alarm -> alarm.getRegionId().equals(regionId));

        if (isExist) {
            user.removeAlarm(regionId);
            userRepository.save(user);
        }
    }


    @Transactional
    public void withdrawUser(String email, String inputPassword, HttpServletRequest httpRequest) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));

        if (!passwordEncoder.matches(inputPassword, user.getPasswordHash())) {
            throw new org.springframework.security.access.AccessDeniedException("비밀번호가 일치하지 않습니다.");
        }

        //1. DB 삭제
        userRepository.delete(user);

        //2.세션 삭제
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}

