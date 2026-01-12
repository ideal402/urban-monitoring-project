package com.ideal402.urban.domain.repository;

import com.ideal402.urban.domain.entity.User;
import com.ideal402.urban.domain.entity.UserAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAlarmRepository extends JpaRepository<UserAlarm, Long> {
}
