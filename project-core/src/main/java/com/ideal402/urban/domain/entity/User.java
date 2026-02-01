package com.ideal402.urban.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<UserAlarm> alarms = new ArrayList<>();

    public User(String nickname, String email, String passwordHash) {
        this.nickname = nickname;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public void addAlarm(Integer regionId) {
        UserAlarm alarm = new UserAlarm( this, regionId);
        this.alarms.add(alarm);
    }

    public void removeAlarm(Integer regionId) {
        this.alarms.removeIf(alarm -> alarm.getRegionId().equals(regionId));
    }
}
