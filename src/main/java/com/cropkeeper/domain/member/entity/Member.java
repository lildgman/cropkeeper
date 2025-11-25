package com.cropkeeper.domain.member.entity;

import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "contact", length = 20)
    private String contact;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Farm> farms = new ArrayList<>();

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 편의 메서드

    /**
     * 회원 탈퇴 처리 (소프트 삭제)
     *
     * 회원을 삭제하면 소유한 모든 농장도 함께 삭제됩니다.
     * Cascade 설정은 JPA remove()에만 적용되므로,
     * 소프트 삭제 시에는 명시적으로 farms도 삭제해야 합니다.
     */
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();

        // 소유한 모든 농장도 소프트 삭제
        this.farms.forEach(Farm::delete);
    }

    // 탈퇴 여부
    public boolean isDeleted() {
        return this.deleted;
    }

    // 이름 변경
    public void updateName(String name) {
        this.name = name;
    }

    // 연락처 변경
    public void updateContact(String contact) {
        this.contact = contact;
    }

    // 비밀번호 변경
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    //==연관관계 편의 메서드==//

    /**
     * 농장 추가 (양방향 연관관계 설정)
     *
     * @param farm 추가할 농장
     */
    public void addFarm(Farm farm) {
        // 기존에 연결된 회원이 있다면 제거
        if (farm.getMember() != null) {
            farm.getMember().getFarms().remove(farm);
        }

        // 양방향 연관관계 설정
        this.farms.add(farm);
        farm.changeMember(this);
    }

    /**
     * 농장 제거 (양방향 연관관계 해제)
     *
     * @param farm 제거할 농장
     */
    public void removeFarm(Farm farm) {
        this.farms.remove(farm);
        farm.changeMember(null);
    }
}
