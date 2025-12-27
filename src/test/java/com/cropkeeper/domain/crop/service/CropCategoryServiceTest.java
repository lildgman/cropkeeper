package com.cropkeeper.domain.crop.service;

import com.cropkeeper.domain.crop.dto.request.CreateCropCategoryRequest;
import com.cropkeeper.domain.crop.dto.request.UpdateCropCategoryRequest;
import com.cropkeeper.domain.crop.dto.response.CropCategoryResponse;
import com.cropkeeper.domain.crop.entity.CropCategory;
import com.cropkeeper.domain.crop.exception.CropCategoryHasCropsException;
import com.cropkeeper.domain.crop.exception.CropCategoryNotFoundException;
import com.cropkeeper.domain.crop.exception.DuplicateCropCategoryNameException;
import com.cropkeeper.domain.crop.repository.CropCategoryRepository;
import com.cropkeeper.domain.crop.repository.CropTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CropCategoryServiceTest {

    @Mock
    private CropCategoryRepository categoryRepository;

    @Mock
    private CropTypeRepository cropTypeRepository;

    @InjectMocks
    private CropCategoryService cropCategoryService;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_Success() {

        // given
        String categoryName = "야채류";

        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName(categoryName)
                .build();

        CropCategory savedCategory = CropCategory.builder()
                .categoryId(1L)
                .categoryName(categoryName)
                .build();

        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CropCategory.class))).thenReturn(savedCategory);

        // when
        CropCategoryResponse response = cropCategoryService.createCategory(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo(categoryName);

        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
        verify(categoryRepository, times(1)).save(any(CropCategory.class));

    }

    @Test
    @DisplayName("카테고리 생성 실패 - 중복된 카테고리명")
    void createCategory_fail_중복된_카테고리명() {

        // given
        String categoryName = "야채류";

        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName(categoryName)
                .build();

        CropCategory existingCategory = CropCategory.builder()
                .categoryId(1L)
                .categoryName(categoryName)
                .build();

        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(existingCategory));

        // when, then
        assertThatThrownBy(() -> cropCategoryService.createCategory(request))
                .isInstanceOf(DuplicateCropCategoryNameException.class)
                .hasMessageContaining("이미 존재하는 카테고리명입니다");

        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
        verify(categoryRepository, never()).save(any(CropCategory.class));
    }

    @Test
    @DisplayName("카테고리 전체 조회")
    void getAllCategories_Success() {

        // given
        CropCategory category1 = CropCategory.builder()
                .categoryId(1L)
                .categoryName("과채류")
                .build();

        CropCategory category2 = CropCategory.builder()
                .categoryId(1L)
                .categoryName("엽채류")
                .build();

        CropCategory category3 = CropCategory.builder()
                .categoryId(1L)
                .categoryName("근채류")
                .build();

        List<CropCategory> categories = Arrays.asList(category1, category2, category3);
        when(categoryRepository.findAll()).thenReturn(categories);

        // when
        List<CropCategoryResponse> response = cropCategoryService.getAllCategories();

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(3);
        assertThat(response)
                .extracting("categoryName")
                .containsExactly("과채류", "엽채류", "근채류");

        verify(categoryRepository, times(1)).findAll();

    }

    @Test
    @DisplayName("전체 카테고리 목록 조회 - 빈목록")
    void getAllCategories_Success_Empty() {

        // given
        when(cropCategoryService.getAllCategories()).thenReturn(Collections.emptyList());

        // when
        List<CropCategoryResponse> response = cropCategoryService.getAllCategories();

        // then
        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("카테고리 Id로 조회 성공")
    void getCategoryById_Success() {

        // given
        Long categoryId = 1L;
        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName("과채류")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        CropCategoryResponse response = cropCategoryService.getCategoryById(categoryId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo("과채류");

        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("카테고리 ID로 조회 실패 - 존재하지 않는 ID")
    void getCategoryById_fail_존재하지_않는_ID() {

        // given
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropCategoryService.getCategoryById(categoryId))
                .isInstanceOf(CropCategoryNotFoundException.class)
                .hasMessageContaining("작물 카테고리를 찾을 수 없습니다.");

        verify(categoryRepository, times(1)).findById(categoryId);


    }

    @Test
    @DisplayName("카테고리 이름으로 조회 - 성공")
    void getCategoryByName_Success() {

        // given
        String categoryName = "과채류";
        CropCategory category = CropCategory.builder()
                .categoryId(1L)
                .categoryName(categoryName)
                .build();

        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(category));

        // when
        CropCategoryResponse response = cropCategoryService.getCategoryByName(categoryName);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo(categoryName);

        verify(categoryRepository, times(1)).findByCategoryName(categoryName);

    }

    @Test
    @DisplayName("카테고리 이름으로 조회 실패 - 존재하지 않은 카테고리명")
    void getCategoryByName_fail_존재하지_않는_카테고리명() {

        // given
        String categoryName = "없음";
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropCategoryService.getCategoryByName(categoryName))
                .isInstanceOf(CropCategoryNotFoundException.class)
                .hasMessageContaining("작물 카테고리를 찾을 수 없습니다");

        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() {

        // given
        Long categoryId = 1L;
        String oldName = "과채류";
        String newName = "과일류";

        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(oldName)
                .build();

        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName(newName)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(cropTypeRepository.existsByCategoryCategoryId(categoryId)).thenReturn(false);
        when(categoryRepository.findByCategoryName(newName)).thenReturn(Optional.empty());

        // when
        CropCategoryResponse response = cropCategoryService.updateCategory(categoryId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo(newName);

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, times(1)).existsByCategoryCategoryId(categoryId);
        verify(categoryRepository, times(1)).findByCategoryName(newName);
    }

    @Test
    @DisplayName("카테고리 수정 성공 - 동일한 이름으로 요청 시 변경x")
    void updateCategory_동일한이름_변경x() {

        // given
        Long categoryId = 1L;
        String categoryName = "과채류";

        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();

        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName(categoryName)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(cropTypeRepository.existsByCategoryCategoryId(categoryId)).thenReturn(false);

        // when
        CropCategoryResponse response = cropCategoryService.updateCategory(categoryId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo(categoryName);

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, times(1)).existsByCategoryCategoryId(categoryId);
        verify(categoryRepository, never()).findByCategoryName(anyString());

    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않는 카테고리")
    void updateCategory_fail_존재하지_않는_카테고리() {

        // given
        Long categoryId = 999L;
        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName("카테고리")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropCategoryService.updateCategory(categoryId, request))
                .isInstanceOf(CropCategoryNotFoundException.class)
                .hasMessageContaining("작물 카테고리를 찾을 수 없습니다");

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, never()).existsByCategoryCategoryId(anyLong());
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 작물이 존재")
    void updateCategory_fail_작물이_존재() {

        // given
        Long categoryId = 1L;
        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName("과채류")
                .build();

        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName("과일류")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(cropTypeRepository.existsByCategoryCategoryId(categoryId)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> cropCategoryService.updateCategory(categoryId, request))
                .isInstanceOf(CropCategoryHasCropsException.class)
                .hasMessageContaining("해당 카테고리에 연결된 작물이 있어");

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, times(1)).existsByCategoryCategoryId(categoryId);
        verify(categoryRepository, never()).findByCategoryName(anyString());
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 중복된 새 이름")
    void updateCategory_fail_중복된_새_이름() {

        // given
        Long categoryId = 1L;
        String oldName = "과채류";
        String newName = "엽채류";

        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(oldName)
                .build();

        CropCategory existingCategory = CropCategory.builder()
                .categoryId(2L)
                .categoryName(newName)
                .build();

        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName(newName)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(cropTypeRepository.existsByCategoryCategoryId(categoryId)).thenReturn(false);
        when(categoryRepository.findByCategoryName(newName)).thenReturn(Optional.of(existingCategory));

        // when, then
        assertThatThrownBy(() -> cropCategoryService.updateCategory(categoryId, request))
                .isInstanceOf(DuplicateCropCategoryNameException.class)
                .hasMessageContaining("이미 존재하는 카테고리명입니다");

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, times(1)).existsByCategoryCategoryId(categoryId);
        verify(categoryRepository, times(1)).findByCategoryName(newName);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() {

        // given
        Long categoryId = 1L;
        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName("과채류")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(cropTypeRepository.existsByCategoryCategoryId(categoryId)).thenReturn(false);

        // when
        cropCategoryService.deleteCategory(categoryId);

        // then
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, times(1)).existsByCategoryCategoryId(categoryId);
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 존재하지 않는 카테고리")
    void deleteCategory_fail_존재하지않는카테고리() {

        // given
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropCategoryService.deleteCategory(categoryId))
                .isInstanceOf(CropCategoryNotFoundException.class)
                .hasMessageContaining("작물 카테고리를 찾을 수 없습니다");

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, never()).existsByCategoryCategoryId(anyLong());
        verify(categoryRepository, never()).delete(any(CropCategory.class));
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 작물이 존재하는 카테고리")
    void deleteCategory_fail_작물이존재하는카테고리() {

        // given
        Long categoryId = 1L;
        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName("과채류")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(cropTypeRepository.existsByCategoryCategoryId(categoryId)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> cropCategoryService.deleteCategory(categoryId))
                .isInstanceOf(CropCategoryHasCropsException.class)
                .hasMessageContaining("해당 카테고리에 연결된 작물이 있어 작업할 수 없습니다");

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, times(1)).existsByCategoryCategoryId(categoryId);
        verify(categoryRepository, never()).delete(any(CropCategory.class));
    }
}