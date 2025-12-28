package com.cropkeeper.domain.crop.service;

import com.cropkeeper.domain.crop.dto.request.CreateCropTypeRequest;
import com.cropkeeper.domain.crop.dto.response.CropTypeResponse;
import com.cropkeeper.domain.crop.entity.CropCategory;
import com.cropkeeper.domain.crop.entity.CropType;
import com.cropkeeper.domain.crop.exception.CropCategoryNotFoundException;
import com.cropkeeper.domain.crop.exception.DuplicateCropTypeNameException;
import com.cropkeeper.domain.crop.repository.CropCategoryRepository;
import com.cropkeeper.domain.crop.repository.CropTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CropTypeServiceTest {

    @Mock
    private CropTypeRepository cropTypeRepository;

    @Mock
    private CropCategoryRepository cropCategoryRepository;

    @InjectMocks
    private CropTypeService cropTypeService;

    @Test
    @DisplayName("작물 생성 성공")
    void createCropType_Success() {

        // given
        String typeName = "토마토";
        Long categoryId = 1L;
        String categoryName = "과채류";

        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName(typeName)
                .categoryId(categoryId)
                .build();

        CropCategory category = CropCategory.builder()
                .categoryId(1L)
                .categoryName(categoryName)
                .build();

        CropType savedCropType = CropType.builder()
                .typeId(1L)
                .typeName(typeName)
                .category(category)
                .build();

        when(cropTypeRepository.findByCropTypeName(typeName)).thenReturn(Optional.empty());
        when(cropCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(cropTypeRepository.save(any(CropType.class))).thenReturn(savedCropType);

        // when
        CropTypeResponse response = cropTypeService.createCropType(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTypeId()).isEqualTo(1L);
        assertThat(response.getTypeName()).isEqualTo(typeName);
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo(categoryName);

        verify(cropTypeRepository, times(1)).findByCropTypeName(typeName);
        verify(cropCategoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, times(1)).save(any(CropType.class));

    }

    @Test
    @DisplayName("작물 생성 실패 - 중복된 작물명")
    void createCropType_Fail_DuplicateTypeName() {

        // given
        String typeName = "토마토";
        Long categoryId = 1L;
        String categoryName = "과채류";

        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName(typeName)
                .categoryId(categoryId)
                .build();

        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();

        CropType existingCropType = CropType.builder()
                .typeId(1L)
                .typeName(typeName)
                .category(category)
                .build();

        when(cropTypeRepository.findByCropTypeName(typeName)).thenReturn(Optional.of(existingCropType));

        // when, then
        assertThatThrownBy(() -> cropTypeService.createCropType(request))
                .isInstanceOf(DuplicateCropTypeNameException.class)
                .hasMessageContaining("이미 존재하는 작물명입니다");

        verify(cropTypeRepository, times(1)).findByCropTypeName(typeName);
        verify(cropCategoryRepository, never()).findById(anyLong());
        verify(cropTypeRepository, never()).save(any(CropType.class));
    }

    @Test
    @DisplayName("작물 생성 실패 - 카테고리 x")
    void createCropType_Fail_CategoryNotFound() {
        // given
        String typeName = "토마토";
        Long categoryId = 999L;

        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName(typeName)
                .categoryId(categoryId)
                .build();

        when(cropTypeRepository.findByCropTypeName(typeName)).thenReturn(Optional.empty());
        when(cropCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropTypeService.createCropType(request))
                .isInstanceOf(CropCategoryNotFoundException.class)
                .hasMessageContaining("작물 카테고리를 찾을 수 없습니다");

        verify(cropTypeRepository, times(1)).findByCropTypeName(typeName);
        verify(cropCategoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, never()).save(any(CropType.class));
    }
}
