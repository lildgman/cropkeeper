package com.cropkeeper.domain.farm.entity;

import com.cropkeeper.domain.farm.vo.Address;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Embedded
    private Address address;

    @Column(name = "farm_size", nullable = false)
    private Long farmSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 편의 메서드
    public void updateFarmName(String farmName) {
        this.farmName = farmName;
    }

    public void updateAddress(String zipCode, String street, String detail) {
        this.address = Address.updateFrom(this.address, zipCode, street, detail);
    }

    public void updateFarmSize(Long farmSize) {
        this.farmSize = farmSize;
    }

    // 농장 삭제 처리
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제 여부
    public boolean isDeleted() {
        return this.deleted;
    }
}
