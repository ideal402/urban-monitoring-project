package com.ideal402.urban.service;

import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.entity.UserAlarm;
import com.ideal402.urban.domain.repository.UserAlarmRepository;
import com.ideal402.urban.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAlarmRepository userAlarmRepo;
    private final UserRepository userRepo;

    @Transactional
    public void save(User user, Integer regionId) {

        UserAlarm userAlarm = new UserAlarm(user, regionId);

        userAlarmRepo.save(userAlarm);
    }

    @Transactional
    public void delete(User user, Integer regionId) {
        userAlarmRepo.deleteByUserAndRegionId(user, regionId);
    }

    @Transactional
    public void withdrawUser(User principal, String inputPassword) {
        User user = userRepo.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));
        if(!user.getPasswordHash().equals(inputPassword)) {
            throw new org.springframework.security.access.AccessDeniedException("비밀번호가 일치하지 않습니다.");
        }

        userRepo.delete(user);
    }
}

