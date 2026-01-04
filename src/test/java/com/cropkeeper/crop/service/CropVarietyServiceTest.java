package com.cropkeeper.crop.service;

import com.cropkeeper.crop.dto.request.CreateCropVarietyRequest;
import com.cropkeeper.crop.dto.response.CropVarietyResponse;
import com.cropkeeper.crop.entity.CropCategory;
import com.cropkeeper.crop.entity.CropType;
import com.cropkeeper.crop.entity.CropVariety;
import com.cropkeeper.crop.exception.CropTypeNotFoundException;
import com.cropkeeper.crop.exception.DuplicateCropVarietyNameException;
import com.cropkeeper.crop.repository.CropTypeRepository;
import com.cropkeeper.crop.repository.CropVarietyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CropVarietyServiceTest {

    @Mock
    CropVarietyRepository cropVarietyRepository;

    @Mock
    CropTypeRepository cropTypeRepository;

    @InjectMocks
    CropVarietyService cropVarietyService;

    @Test
    @DisplayName("품종 생성 성공")
    void createCropVariety_Success() {

        // given
        Long typeId = 1L;
        String varietyName = "대추토마토";

        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .typeId(typeId)
                .varietyName(varietyName)
                .build();

        // 카테고리
        CropCategory category = CropCategory.builder()
                .categoryId(1L)
                .categoryName("과채류")
                .build();

        // 작물
        CropType cropType = CropType.builder()
                .typeId(typeId)
                .typeName("방울토마토")
                .category(category)
                .build();

        // 품종
        CropVariety variety = CropVariety.builder()
                .varietyId(1L)
                .cropType(cropType)
                .varietyName(varietyName)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(cropType));
        when(cropVarietyRepository.existsByVarietyNameAndDeletedFalse(varietyName)).thenReturn(false);
        when(cropVarietyRepository.save(any(CropVariety.class))).thenReturn(variety);

        // when
        CropVarietyResponse response = cropVarietyService.createCropVariety(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getVarietyId()).isEqualTo(1L);
        assertThat(response.getVarietyName()).isEqualTo(varietyName);
        assertThat(response.getTypeId()).isEqualTo(typeId);
        assertThat(response.getTypeName()).isEqualTo("방울토마토");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("과채류");

    }

    @Test
    @DisplayName("품종 생성 실패 - 존재하지 않는 작물")
    void createCropVariety_Fail_CropTypeNotFound() {

        // given
        Long nonExistentTypeId = 999L;
        String varietyName = "대추토마토";

        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .typeId(nonExistentTypeId)
                .varietyName(varietyName)
                .build();

        when(cropTypeRepository.findById(nonExistentTypeId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropVarietyService.createCropVariety(request))
                .isInstanceOf(CropTypeNotFoundException.class)
                .hasMessageContaining("존재하지 않는 작물입니다");

        verify(cropTypeRepository, times(1)).findById(nonExistentTypeId);
        verify(cropVarietyRepository, never()).existsByVarietyNameAndDeletedFalse(varietyName);
        verify(cropVarietyRepository, never()).save(any(CropVariety.class));
    }

    @Test
    @DisplayName("품종 생성 실패 - 품종명 중복")
    void createCropVariety_Fail_DuplicateVarietyName() {

        // given
        Long typeId = 1L;
        String duplicateVarietyName = "대추방울토마토";

        // request
        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .typeId(1L)
                .varietyName(duplicateVarietyName)
                .build();

        // 카테고리
        CropCategory category = CropCategory.builder()
                .categoryId(1L)
                .categoryName("과채류")
                .build();

        // 작물
        CropType cropType = CropType.builder()
                .typeId(typeId)
                .typeName("방울토마토")
                .category(category)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(cropType));
        when(cropVarietyRepository.existsByVarietyNameAndDeletedFalse(duplicateVarietyName)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> cropVarietyService.createCropVariety(request))
                .isInstanceOf(DuplicateCropVarietyNameException.class)
                .hasMessageContaining("이미 존재하는 품종명입니다");

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropVarietyRepository, times(1)).existsByVarietyNameAndDeletedFalse(duplicateVarietyName);
        verify(cropVarietyRepository, never()).save(any(CropVariety.class));
    }


}
