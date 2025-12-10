package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.Crop;
import com.cropkeeper.domain.crop.entity.CropCategory;
import com.cropkeeper.domain.crop.entity.CropVariety;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CropVarietyRepositoryTest {

    @Autowired
    private CropVarietyRepository cropVarietyRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CropCategory category;
    private Crop crop1;
    private Crop crop2;

    @BeforeEach
    void setUp() {
        // Given: 테스트용 카테고리 및 작물 생성
        category = CropCategory.builder()
                .categoryName("채소류")
                .build();
        category = entityManager.persist(category);

        crop1 = Crop.builder()
                .cropName("토마토")
                .category(category)
                .build();
        crop1 = entityManager.persist(crop1);

        crop2 = Crop.builder()
                .cropName("오이")
                .category(category)
                .build();
        crop2 = entityManager.persist(crop2);

        entityManager.flush();
    }

    @Test
    @DisplayName("품종 저장 성공")
    void save_Success() {
        // Given: 새로운 품종 엔티티 생성
        CropVariety variety = CropVariety.builder()
                .varietyName("방울토마토")
                .crop(crop1)
                .build();

        // When: 품종 저장
        CropVariety savedVariety = cropVarietyRepository.save(variety);
        entityManager.flush();
        entityManager.clear();

        // Then: 저장된 품종 검증
        assertThat(savedVariety.getVarietyId()).isNotNull();
        assertThat(savedVariety.getVarietyName()).isEqualTo("방울토마토");
        assertThat(savedVariety.getCrop().getCropId()).isEqualTo(crop1.getCropId());
        assertThat(savedVariety.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("작물ID와 품종명으로 조회 성공")
    void findByCropIdAndVarietyName_Success() {
        // Given: 품종 저장
        CropVariety variety = CropVariety.builder()
                .varietyName("대추토마토")
                .crop(crop1)
                .build();
        entityManager.persist(variety);
        entityManager.flush();
        entityManager.clear();

        // When: 작물ID와 품종명으로 조회
        Optional<CropVariety> foundVariety = cropVarietyRepository
                .findByCrop_CropIdAndVarietyName(crop1.getCropId(), "대추토마토");

        // Then: 조회 결과 검증
        assertThat(foundVariety).isPresent();
        assertThat(foundVariety.get().getVarietyName()).isEqualTo("대추토마토");
        assertThat(foundVariety.get().getCrop().getCropName()).isEqualTo("토마토");
    }

    @Test
    @DisplayName("존재하지 않는 작물ID로 조회 시 Empty 반환")
    void findByCropIdAndVarietyName_NotFoundCropId() {
        // Given: 품종 저장
        CropVariety variety = CropVariety.builder()
                .varietyName("방울토마토")
                .crop(crop1)
                .build();
        entityManager.persist(variety);
        entityManager.flush();

        // When: 존재하지 않는 작물ID로 조회
        Optional<CropVariety> foundVariety = cropVarietyRepository
                .findByCrop_CropIdAndVarietyName(999L, "방울토마토");

        // Then: Empty Optional 반환
        assertThat(foundVariety).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 품종명으로 조회 시 Empty 반환")
    void findByCropIdAndVarietyName_NotFoundVarietyName() {
        // Given: 품종 저장
        CropVariety variety = CropVariety.builder()
                .varietyName("방울토마토")
                .crop(crop1)
                .build();
        entityManager.persist(variety);
        entityManager.flush();

        // When: 존재하지 않는 품종명으로 조회
        Optional<CropVariety> foundVariety = cropVarietyRepository
                .findByCrop_CropIdAndVarietyName(crop1.getCropId(), "존재하지않는품종");

        // Then: Empty Optional 반환
        assertThat(foundVariety).isEmpty();
    }

    @Test
    @DisplayName("다른 작물에 같은 품종명 저장 성공 - composite unique 확인")
    void save_SameVarietyNameDifferentCrop_Success() {
        // Given: crop1에 "청과" 품종 저장
        CropVariety variety1 = CropVariety.builder()
                .varietyName("청과")
                .crop(crop1)
                .build();
        cropVarietyRepository.save(variety1);
        entityManager.flush();

        // When: crop2에 동일한 품종명 "청과" 저장
        CropVariety variety2 = CropVariety.builder()
                .varietyName("청과")  // 같은 품종명
                .crop(crop2)          // 다른 작물
                .build();
        CropVariety savedVariety2 = cropVarietyRepository.save(variety2);
        entityManager.flush();

        // Then: 다른 작물이므로 정상 저장됨
        assertThat(savedVariety2.getVarietyId()).isNotNull();
        assertThat(savedVariety2.getVarietyName()).isEqualTo("청과");
        assertThat(savedVariety2.getCrop().getCropId()).isEqualTo(crop2.getCropId());
    }

    @Test
    @DisplayName("중복된 작물+품종명 저장 시도 시 예외 발생")
    void save_DuplicateCropAndVarietyName_ThrowsException() {
        // Given: 첫 번째 품종 저장
        CropVariety variety1 = CropVariety.builder()
                .varietyName("방울토마토")
                .crop(crop1)
                .build();
        cropVarietyRepository.save(variety1);
        entityManager.flush();

        // When & Then: 동일한 작물+품종명 조합으로 저장 시도 시 예외 발생
        CropVariety variety2 = CropVariety.builder()
                .varietyName("방울토마토")  // 중복된 품종명
                .crop(crop1)                 // 동일한 작물
                .build();

        assertThatThrownBy(() -> {
            cropVarietyRepository.save(variety2);
            entityManager.flush();  // flush 시점에 composite unique 제약 조건 검증
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
