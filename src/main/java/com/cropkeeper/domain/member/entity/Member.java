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
    @Column(name = "user_id")
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

    // 회원 탈퇴 처리
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 탈퇴 여부
    public boolean isDeleted() {
        return this.deleted;
    }

    // 회원 정보 변경
    public void updateInfo(String name, String contact) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }

        if (contact != null && !contact.isEmpty()) {
            this.contact = contact;
        }
    }

    // 비밀번호 변경
    public void changePassword(String encodedPasssword) {
        this.password = encodedPasssword;
    }
}
