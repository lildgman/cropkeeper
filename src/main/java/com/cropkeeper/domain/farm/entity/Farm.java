package com.cropkeeper.domain.farm.entity;

import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "farm")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Farm extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farm_id")
    private Long farmId;

    @Column(name = "farm_name", nullable = false, length = 100)
    private String farmName;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "farm_size", nullable = false)
    private Long farmSize;


}
