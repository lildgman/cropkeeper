package com.cropkeeper.domain.farminglog.entity;

import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.user.entity.Users;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "farming_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FarmingLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Column(name = "log_date", nullable = false)
    private LocalDateTime logDate;

    @Column(name = "weather", nullable = false)
    private String weather;

    @Column(name = "temperature")
    private Long temperature;

    @Column(name = "humidity")
    private Long humidity;

    @Column(name = "memo", length = 255)
    private String memo;

    @OneToMany(mappedBy = "farmingLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FarmingLogImage> images;
}
