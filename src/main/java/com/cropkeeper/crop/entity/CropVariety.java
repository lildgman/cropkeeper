package com.cropkeeper.crop.entity;

import com.cropkeeper.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crop_variety",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_crop_variety",
                columnNames = {"crop_type_id", "variety_name"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CropVariety extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variety_id")
    private Long varietyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_type_id", nullable = false)
    private CropType cropType;

    @Column(name = "variety_name", nullable = false, length = 20)
    private String varietyName;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 작물 이름 수정
     *
     * @param newName
     */
    public void updateCropVarietyName(String newName) {
        this.varietyName = newName;
    }

    public boolean isDeleted() {
        return this.deleted;
    }
}
