package com.cropkeeper.crop.repository;

import com.cropkeeper.crop.entity.CropType;
import com.cropkeeper.crop.entity.CropCategory;
import com.cropkeeper.crop.repository.CropTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CropTypeRepositoryTest {

    @Autowired
    private CropTypeRepository cropTypeRepository;

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
        CropType cropType = CropType.builder()
                .typeName("토마토")
                .category(category)
                .build();

        // When: 작물 저장
        CropType savedCropType = cropTypeRepository.save(cropType);
        em.flush();
        em.clear();

        // Then: 저장된 작물 검증
        assertThat(savedCropType.getTypeId()).isNotNull();
        assertThat(savedCropType.getTypeName()).isEqualTo("토마토");
        assertThat(savedCropType.getCategory().getCategoryId()).isEqualTo(category.getCategoryId());
        assertThat(savedCropType.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("작물명으로 조회 성공")
    void findByCropTypeName_Success() {
        // Given: 작물 저장
        CropType cropType = CropType.builder()
                .typeName("오이")
                .category(category)
                .build();
        em.persist(cropType);
        em.flush();
        em.clear();

        // When: 작물명으로 조회
        Optional<CropType> foundCrop = cropTypeRepository.findByCropTypeName("오이");

        // Then: 조회 결과 검증
        assertThat(foundCrop).isPresent();
        assertThat(foundCrop.get().getTypeName()).isEqualTo("오이");
        assertThat(foundCrop.get().getCategory().getCategoryName()).isEqualTo("채소류");
    }

    @Test
    @DisplayName("존재하지 않는 작물명 조회 시 Empty 반환")
    void findByCropTypeName_NotFound() {
        // Given: 데이터 없음

        // When: 존재하지 않는 작물명으로 조회
        Optional<CropType> foundCrop = cropTypeRepository.findByCropTypeName("존재하지않는작물");

        // Then: Empty Optional 반환
        assertThat(foundCrop).isEmpty();
    }

    @Test
    @DisplayName("작물 ID로 조회 성공")
    void findById_Success() {
        // Given: 작물 저장
        CropType cropType = CropType.builder()
                .typeName("상추")
                .category(category)
                .build();
        cropType = em.persist(cropType);
        em.flush();
        em.clear();

        // When: ID로 조회
        Optional<CropType> foundCrop = cropTypeRepository.findById(cropType.getTypeId());

        // Then: 조회 결과 검증
        assertThat(foundCrop).isPresent();
        assertThat(foundCrop.get().getTypeId()).isEqualTo(cropType.getTypeId());
        assertThat(foundCrop.get().getTypeName()).isEqualTo("상추");
    }

    @Test
    @DisplayName("중복된 작물명 저장 시도 시 예외 발생")
    void save_DuplicateCropName_ThrowsException() {
        // Given: 동일한 작물명으로 첫 번째 작물 저장
        CropType cropType1 = CropType.builder()
                .typeName("토마토")
                .category(category)
                .build();
        cropTypeRepository.save(cropType1);
        em.flush();

        // When & Then: 중복된 작물명으로 저장 시도 시 예외 발생
        CropType cropType2 = CropType.builder()
                .typeName("토마토")  // 중복된 작물명
                .category(category)
                .build();

        assertThatThrownBy(() -> {
            cropTypeRepository.save(cropType2);
            em.flush();  // flush 시점에 unique 제약 조건 검증
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("전체 작물 조회")
    void findAllByDeletedFalse_Success() {

        // given
        CropType cropType1 = CropType.builder()
                .typeName("토마토")
                .category(category)
                .build();

        CropType cropType2 = CropType.builder()
                .typeName("오이")
                .category(category)
                .build();

        CropType cropType3 = CropType.builder()
                .typeName("상추")
                .category(category)
                .build();

        em.persist(cropType1);
        em.persist(cropType2);
        em.persist(cropType3);
        em.flush();

        cropType3.delete();
        em.flush();
        em.clear();

        // when
        List<CropType> cropTypes = cropTypeRepository.findAllByDeletedFalse();

        // then
        assertThat(cropTypes).hasSize(2);
        assertThat(cropTypes)
                .extracting(CropType::getTypeName)
                .containsExactlyInAnyOrder("토마토", "오이");
        assertThat(cropTypes).allMatch(ct -> !ct.isDeleted());
    }

    @Test
    @DisplayName("전체 작물 조회 - 빈목록")
    void findAllByDeletedFalse_EmtpyList() {

        // when
        List<CropType> cropTypes = cropTypeRepository.findAllByDeletedFalse();
        // then
        assertThat(cropTypes).isEmpty();

    }

    @Test
    @DisplayName("작물 id로 조회 - 존재하지 않는 ID")
    void findById_존재하지않는id_empty() {

        // when
        Optional<CropType> foundCropType = cropTypeRepository.findById(999L);

        // then
        assertThat(foundCropType).isEmpty();
    }

    @Test
    @DisplayName("작물 ID 조회 - 삭제된 작물은 empty")
    void findById_삭제된CropType() {

        // given
        CropType cropType = CropType.builder()
                .typeName("토마토")
                .category(category)
                .build();

        cropType = em.persist(cropType);
        em.flush();

        Long typeId = cropType.getTypeId();

        // 삭제처리
        cropType.delete();
        em.flush();
        em.clear();

        // when
        Optional<CropType> foundCropType = cropTypeRepository.findById(typeId);

        // then
        assertThat(foundCropType).isEmpty();
    }

    @Test
    @DisplayName("카테고리별 작물 조회 - 특정 카테고리 작물 조회")
    void findByCategoryCategoryIdAndDeleteFalse_특정카테고리CropType조회() {

        // given
        // 두번째 카테고리
        CropCategory category2 = CropCategory.builder()
                .categoryName("과일류")
                .build();
        category2 = em.persist(category2);

        // 첫번째 카테고리에 속할 작물
        CropType cropType1 = CropType.builder()
                .typeName("토마토")
                .category(category)
                .build();

        CropType cropType2 = CropType.builder()
                .typeName("아스파라거스")
                .category(category)
                .build();

        // 두번째 카테고리에 속할 작물
        CropType cropType3 = CropType.builder()
                .typeName("딸기")
                .category(category2)
                .build();

        em.persist(cropType1);
        em.persist(cropType2);
        em.persist(cropType3);
        em.flush();
        em.clear();

        // when
        List<CropType> cropTypes = cropTypeRepository.findByCategoryCategoryIdAndDeletedFalse(category.getCategoryId());

        // then
        assertThat(cropTypes).hasSize(2);
        assertThat(cropTypes)
                .extracting(CropType::getTypeName)
                .containsExactlyInAnyOrder("토마토", "아스파라거스");
        assertThat(cropTypes)
                .allMatch(ct -> ct.getCategory().getCategoryId().equals(category.getCategoryId()));
    }

    @Test
    @DisplayName("카테고리별 작물 조회 - 해당 카테고리에 작물이 없으면 빈 리스트반환")
    void findByCategoryCategoryIdAndDeletedFalse_CropType없음_빈리스트() {

        // when
        List<CropType> cropTypes = cropTypeRepository.findByCategoryCategoryIdAndDeletedFalse(category.getCategoryId());

        // then
        assertThat(cropTypes).isEmpty();
    }

    @Test
    @DisplayName("카테고리별 작물 조회 - 삭제된 작물 제외 후 조회")
    void findByCategoryCategoryIdAndDeletedFalse_삭제된CropType제외() {

        // given
        CropType cropType1 = CropType.builder()
                .typeName("토마토")
                .category(category)
                .build();

        CropType cropType2 = CropType.builder()
                .typeName("오이")
                .category(category)
                .build();

        em.persist(cropType1);
        em.persist(cropType2);
        em.flush();

        cropType2.delete();
        em.flush();
        em.clear();

        // when
        List<CropType> cropTypes = cropTypeRepository.findByCategoryCategoryIdAndDeletedFalse(category.getCategoryId());

        // then
        assertThat(cropTypes).hasSize(1);

    }
}
