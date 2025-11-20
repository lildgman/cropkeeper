package com.cropkeeper.domain.farm.entity;

import com.cropkeeper.domain.member.entity.Member;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 편의 메서드
    public void updateInfo(String farmName, String address, Long farmSize) {

        if (farmName != null && !farmName.isEmpty()) {
            this.farmName = farmName;
        }

        if (address != null && !address.isEmpty()) {
            this.address = address;
        }

        if (farmSize != null && farmSize > 0) {
            this.farmSize = farmSize;
        }
    }
}
