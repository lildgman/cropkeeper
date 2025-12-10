package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.CropCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CropCategoryRepositoryTest {

    @Autowired
    private CropCategoryRepository cropCategoryRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("카테고리 이름으로 조회")
    void findByCategoryName_Success() {

        // given
        CropCategory category = CropCategory.builder()
                .categoryName("채소")
                .build();
        em.persist(category);
        em.flush();

        // when
        Optional<CropCategory> found = cropCategoryRepository.findByCategoryName("채소");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getCategoryName()).isEqualTo("채소");

    }

    @Test
    @DisplayName("카테고리 이름으로 조회 실패 - 없는 카테고리")
    void findByCategory_NotFound() {

        Optional<CropCategory> result = cropCategoryRepository.findByCategoryName("없엉");

        assertThat(result).isEmpty();
    }
}