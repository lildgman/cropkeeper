package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.Crop;
import com.cropkeeper.domain.crop.entity.CropCategory;
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
class CropRepositoryTest {

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private TestEntityManager em;

    private CropCategory category;

    @BeforeEach
    void setUp() {
        // Given: 테스트용 작물 카테고리 생성
        category = CropCategory.builder()
                .categoryName("채소류")
                .build();
        category = em.persist(category);
        em.flush();
    }

    @Test
    @DisplayName("작물 저장 성공")
    void save_Success() {
        // Given: 새로운 작물 엔티티 생성
        Crop crop = Crop.builder()
                .cropName("토마토")
                .category(category)
                .build();

        // When: 작물 저장
        Crop savedCrop = cropRepository.save(crop);
        em.flush();
        em.clear();

        // Then: 저장된 작물 검증
        assertThat(savedCrop.getCropId()).isNotNull();
        assertThat(savedCrop.getCropName()).isEqualTo("토마토");
        assertThat(savedCrop.getCategory().getCategoryId()).isEqualTo(category.getCategoryId());
        assertThat(savedCrop.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("작물명으로 조회 성공")
    void findByCropName_Success() {
        // Given: 작물 저장
        Crop crop = Crop.builder()
                .cropName("오이")
                .category(category)
                .build();
        em.persist(crop);
        em.flush();
        em.clear();

        // When: 작물명으로 조회
        Optional<Crop> foundCrop = cropRepository.findByCropName("오이");

        // Then: 조회 결과 검증
        assertThat(foundCrop).isPresent();
        assertThat(foundCrop.get().getCropName()).isEqualTo("오이");
        assertThat(foundCrop.get().getCategory().getCategoryName()).isEqualTo("채소류");
    }

    @Test
    @DisplayName("존재하지 않는 작물명 조회 시 Empty 반환")
    void findByCropName_NotFound() {
        // Given: 데이터 없음

        // When: 존재하지 않는 작물명으로 조회
        Optional<Crop> foundCrop = cropRepository.findByCropName("존재하지않는작물");

        // Then: Empty Optional 반환
        assertThat(foundCrop).isEmpty();
    }

    @Test
    @DisplayName("작물 ID로 조회 성공")
    void findById_Success() {
        // Given: 작물 저장
        Crop crop = Crop.builder()
                .cropName("상추")
                .category(category)
                .build();
        crop = em.persist(crop);
        em.flush();
        em.clear();

        // When: ID로 조회
        Optional<Crop> foundCrop = cropRepository.findById(crop.getCropId());

        // Then: 조회 결과 검증
        assertThat(foundCrop).isPresent();
        assertThat(foundCrop.get().getCropId()).isEqualTo(crop.getCropId());
        assertThat(foundCrop.get().getCropName()).isEqualTo("상추");
    }

    @Test
    @DisplayName("중복된 작물명 저장 시도 시 예외 발생")
    void save_DuplicateCropName_ThrowsException() {
        // Given: 동일한 작물명으로 첫 번째 작물 저장
        Crop crop1 = Crop.builder()
                .cropName("토마토")
                .category(category)
                .build();
        cropRepository.save(crop1);
        em.flush();

        // When & Then: 중복된 작물명으로 저장 시도 시 예외 발생
        Crop crop2 = Crop.builder()
                .cropName("토마토")  // 중복된 작물명
                .category(category)
                .build();

        assertThatThrownBy(() -> {
            cropRepository.save(crop2);
            em.flush();  // flush 시점에 unique 제약 조건 검증
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
