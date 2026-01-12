package com.ideal402.urban.domain.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_alarms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_region",
                        columnNames = {"user_id", "region_id"}
                )
        }
)
public class UserAlarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="user_id", nullable = false)
    private User user;

    @Column(name = "region_id", nullable = false)
    private Integer regionId;

    public UserAlarm(User user, Integer regionId) {
        this.regionId = regionId;
        this.user = user;
    }
}
