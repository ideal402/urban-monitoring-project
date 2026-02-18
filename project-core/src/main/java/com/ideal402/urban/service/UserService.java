package com.ideal402.urban.service;

import com.ideal402.urban.domain.entity.Region;
import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.entity.UserAlarm;
import com.ideal402.urban.domain.repository.RegionRepository;
import com.ideal402.urban.domain.repository.UserAlarmRepository;
import com.ideal402.urban.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAlarmRepository userAlarmRepository;
    private final RegionRepository regionRepository;
    private final PasswordEncoder passwordEncoder;

    public void addAlarm(String email, Integer regionId) {

        Region region = regionRepository.findById(regionId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userAlarmRepository.existsByUserAndRegionId(user, regionId)) {
            return;
        }

        UserAlarm userAlarm = new UserAlarm(user, regionId);
        userAlarmRepository.save(userAlarm);
    }


    public void deleteAlarm(String email, Integer regionId) {

        Region region = regionRepository.findById(regionId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userAlarmRepository.findByUserAndRegionId(user, regionId)
                .ifPresent(userAlarmRepository::delete);

    }


    @Transactional
    public void withdrawUser(String email, String inputPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));

        if (!passwordEncoder.matches(inputPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userAlarmRepository.deleteByUser(user);
        userRepository.delete(user);
    }
}

