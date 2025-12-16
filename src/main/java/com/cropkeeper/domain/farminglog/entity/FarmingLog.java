package com.cropkeeper.domain.farminglog.entity;

import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farminglog.vo.FarmingMetadata;
import com.cropkeeper.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "farming_log")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "log_type", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public abstract class FarmingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farming_log_id")
    private Long farmingLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Embedded
    private FarmingMetadata metadata;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 공통 편의 메서드
    public void updateMetadata(FarmingMetadata metadata) {
        this.metadata = metadata;
    }

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deleted;
    }
}
