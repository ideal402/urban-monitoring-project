package com.ideal402.urban.domain.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_alarms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAlarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer regionId;

    public UserAlarm(Integer regionId, User user) {
        this.regionId = regionId;
        this.user = user;
    }
}
