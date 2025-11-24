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

    // 회원 탈퇴 처리
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
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
}
