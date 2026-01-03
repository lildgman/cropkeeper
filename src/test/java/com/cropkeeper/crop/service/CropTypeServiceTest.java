package com.cropkeeper.crop.service;

import com.cropkeeper.crop.dto.request.CreateCropTypeRequest;
import com.cropkeeper.crop.dto.request.UpdateCropTypeRequest;
import com.cropkeeper.crop.dto.response.CropTypeResponse;
import com.cropkeeper.crop.entity.CropCategory;
import com.cropkeeper.crop.entity.CropType;
import com.cropkeeper.crop.exception.CropCategoryNotFoundException;
import com.cropkeeper.crop.exception.CropTypeHasVarietiesException;
import com.cropkeeper.crop.exception.CropTypeNotFoundException;
import com.cropkeeper.crop.exception.DuplicateCropTypeNameException;
import com.cropkeeper.crop.exception.InvalidCropRequestException;
import com.cropkeeper.crop.repository.CropCategoryRepository;
import com.cropkeeper.crop.repository.CropTypeRepository;
import com.cropkeeper.crop.repository.CropVarietyRepository;
import com.cropkeeper.crop.service.CropTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

    @Mock
    private CropVarietyRepository cropVarietyRepository;

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

        when(cropTypeRepository.existsByTypeNameAndDeletedFalse(typeName)).thenReturn(false);
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

        verify(cropTypeRepository, times(1)).existsByTypeNameAndDeletedFalse(typeName);
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

        when(cropTypeRepository.existsByTypeNameAndDeletedFalse(typeName)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> cropTypeService.createCropType(request))
                .isInstanceOf(DuplicateCropTypeNameException.class)
                .hasMessageContaining("이미 존재하는 작물명입니다");

        verify(cropTypeRepository, times(1)).existsByTypeNameAndDeletedFalse(typeName);
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

        when(cropTypeRepository.existsByTypeNameAndDeletedFalse(typeName)).thenReturn(false);
        when(cropCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropTypeService.createCropType(request))
                .isInstanceOf(CropCategoryNotFoundException.class)
                .hasMessageContaining("작물 카테고리를 찾을 수 없습니다");

        verify(cropTypeRepository, times(1)).existsByTypeNameAndDeletedFalse(typeName);
        verify(cropCategoryRepository, times(1)).findById(categoryId);
        verify(cropTypeRepository, never()).save(any(CropType.class));
    }

    @Test
    @DisplayName("전체 작물 목록 조회")
    void getAllCropTypes_Success() {

        // given
        CropCategory category1 = CropCategory.builder()
                .categoryId(1L)
                .categoryName("과채류")
                .build();

        CropCategory category2 = CropCategory.builder()
                .categoryId(2L)
                .categoryName("엽채류")
                .build();

        CropType cropType1 = CropType.builder()
                .typeId(1L)
                .typeName("토마토")
                .category(category1)
                .build();

        CropType cropType2 = CropType.builder()
                .typeId(2L)
                .typeName("오이")
                .category(category1)
                .build();

        CropType cropType3 = CropType.builder()
                .typeId(3L)
                .typeName("상추")
                .category(category2)
                .build();

        List<CropType> cropTypes = List.of(cropType1, cropType2, cropType3);

        when(cropTypeRepository.findAllByDeletedFalse()).thenReturn(cropTypes);

        // when
        List<CropTypeResponse> responses = cropTypeService.getAllCropTypes();

        // then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0).getTypeName()).isEqualTo("토마토");
        assertThat(responses.get(1).getTypeName()).isEqualTo("오이");
        assertThat(responses.get(2).getTypeName()).isEqualTo("상추");

        verify(cropTypeRepository, times(1)).findAllByDeletedFalse();

    }


    @Test
    @DisplayName("전체 작물 목록 조회 성공 - 빈 목록")
    void getAllCropTypes_EmptyList() {

        // given
        when(cropTypeRepository.findAllByDeletedFalse()).thenReturn(List.of());

        // when
        List<CropTypeResponse> responses = cropTypeService.getAllCropTypes();

        // then
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(cropTypeRepository, times(1)).findAllByDeletedFalse();
    }

    @Test
    @DisplayName("작물 ID로 조회 성공")
    void getCropTypeById_Success() {

        // given
        Long typeId = 1L;
        String typeName = "토마토";

        CropCategory category = CropCategory.builder()
                .categoryId(1L)
                .categoryName("과채류")
                .build();

        CropType cropType = CropType.builder()
                .typeId(typeId)
                .typeName(typeName)
                .category(category)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(cropType));

        // when
        CropTypeResponse response = cropTypeService.getCropTypeById(typeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTypeId()).isEqualTo(typeId);
        assertThat(response.getTypeName()).isEqualTo(typeName);
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("과채류");
    }

    @Test
    @DisplayName("작물 ID로 조회 실패 - 존재하지 않는 작물")
    void getCropTypeById_Fail_NotFound() {

        // given
        Long typeId = 999L;

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropTypeService.getCropTypeById(typeId))
                .isInstanceOf(CropTypeNotFoundException.class)
                .hasMessageContaining("작물을 찾을 수 없습니다");

        verify(cropTypeRepository, times(1)).findById(typeId);
    }

    @Test
    @DisplayName("카테고리별 작물 목록 조회 성공")
    void getCropTypesByCategoryId_Success() {

        // given
        // 카테고리 생성
        Long categoryId = 1L;
        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName("과채류")
                .build();

        // 카테고리에 속할 작물 생성
        CropType cropType1 = CropType.builder()
                .typeId(1L)
                .typeName("토마토")
                .category(category)
                .build();

        CropType cropType2 = CropType.builder()
                .typeId(2L)
                .typeName("오이")
                .category(category)
                .build();

        List<CropType> cropTypes = List.of(cropType1, cropType2);

        when(cropTypeRepository.findByCategoryCategoryIdAndDeletedFalse(categoryId)).thenReturn(cropTypes);

        // when
        List<CropTypeResponse> responses = cropTypeService.getCropTypesByCategoryId(categoryId);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);

        verify(cropTypeRepository, times(1)).findByCategoryCategoryIdAndDeletedFalse(categoryId);
    }

    @Test
    @DisplayName("카테고리별 작물 목록 조회 성공 - 빈목록")
    void getCropTypesByCategoryId_EmptyList() {

        // given
        Long categoryId = 1L;

        when(cropTypeRepository.findByCategoryCategoryIdAndDeletedFalse(categoryId)).thenReturn(List.of());

        // when
        List<CropTypeResponse> responses = cropTypeService.getCropTypesByCategoryId(categoryId);

        // then
        assertThat(responses).isEmpty();

        verify(cropTypeRepository, times(1)).findByCategoryCategoryIdAndDeletedFalse(categoryId);

    }

    @Test
    @DisplayName("작물 id로 조회 실패 - 삭제된 작물")
    void getCropTypesById_Fail_DeletedCropType() {

        // given
        Long typeId = 1L;

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropTypeService.getCropTypeById(typeId))
                .isInstanceOf(CropTypeNotFoundException.class)
                .hasMessageContaining("작물을 찾을 수 없습니다");

        verify(cropTypeRepository, times(1)).findById(typeId);
    }

    @Test
    @DisplayName("작물 수정 성공 - typeName만 변경")
    void updateCropType_typeNameOnly_Success() {

        // given
        // 기존 작물 등록
        Long typeId = 1L;
        String originalName = "토마토";

        String newName = "방울토마토";

        Long categoryId = 1L;
        String categoryName = "과채류";

        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();

        CropType existingCropType = CropType.builder()
                .typeId(typeId)
                .typeName(originalName)
                .category(category)
                .build();

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(newName)
                .categoryId(null)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(existingCropType));
        when(cropTypeRepository.existsByTypeNameAndDeletedFalse(newName)).thenReturn(false);

        // when
        CropTypeResponse response = cropTypeService.updateCropType(typeId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTypeId()).isEqualTo(typeId);
        assertThat(response.getTypeName()).isEqualTo(newName);
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo(categoryName);

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropTypeRepository, times(1)).existsByTypeNameAndDeletedFalse(newName);

    }

    @Test
    @DisplayName("작물 수정 성공 - categoryId만 변경")
    void updateCropType_CategoryIdOnly_Success() {

        // given
        // 기존 작물
        Long typeId = 1L;
        String typeName = "토마토";

        // 기존 작물의 카테고리
        Long categoryId = 1L;
        String oldCategoryName = "과채류";

        // 변경할 카테고리
        Long newCategoryId = 2L;
        String newCategoryName = "엽채류";

        CropCategory oldCategory = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(oldCategoryName)
                .build();

        CropCategory newCategory = CropCategory.builder()
                .categoryId(newCategoryId)
                .categoryName(newCategoryName)
                .build();

        CropType cropType = CropType.builder()
                .typeId(typeId)
                .typeName(typeName)
                .category(oldCategory)
                .build();

        // 수정 request - 카테고리만
        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(null)
                .categoryId(2L)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(cropType));
        when(cropCategoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));

        // when
        CropTypeResponse response = cropTypeService.updateCropType(typeId, request);

        // then
        assertThat(response.getTypeId()).isEqualTo(typeId);
        assertThat(response.getTypeName()).isEqualTo(typeName);
        assertThat(response.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(response.getCategoryName()).isEqualTo(newCategoryName);

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropCategoryRepository, times(1)).findById(newCategoryId);
        verify(cropTypeRepository, never()).existsByTypeNameAndDeletedFalse(anyString());
    }

    @Test
    @DisplayName("작물 수정 성공 - typeName, category 둘다")
    void updateCropType_both_Success() {

        // given
        // 기존 작물
        Long typeId = 1L;
        String typeName = "토마토";

        // 작물 카테고리
        Long categoryId = 1L;
        String categoryName = "과채류";

        // 변경할 작물
        String newTypeName = "상추";

        // 변경할 작물 카테고리
        Long newCategoryId = 2L;
        String newCategoryName = "엽채류";

        CropCategory oldCategory = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();

        CropCategory newCategory = CropCategory.builder()
                .categoryId(newCategoryId)
                .categoryName(newCategoryName)
                .build();

        CropType cropType = CropType.builder()
                .typeId(typeId)
                .typeName(typeName)
                .category(oldCategory)
                .build();

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(newTypeName)
                .categoryId(newCategoryId)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(cropType));
        when(cropTypeRepository.existsByTypeNameAndDeletedFalse(newTypeName)).thenReturn(false);
        when(cropCategoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));

        // when
        CropTypeResponse response = cropTypeService.updateCropType(typeId, request);

        // then
        assertThat(response.getTypeId()).isEqualTo(typeId);
        assertThat(response.getTypeName()).isEqualTo(newTypeName);
        assertThat(response.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(response.getCategoryName()).isEqualTo(newCategoryName);

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropCategoryRepository, times(1)).findById(newCategoryId);
        verify(cropTypeRepository, times(1)).existsByTypeNameAndDeletedFalse(newTypeName);
    }

    @Test
    @DisplayName("작물 수정 - 동일한 값으로 변경 시도 (변경 없음)")
    void updateCropType_sameValues_NoChange() {

        // given
        Long typeId = 1L;
        String typeName = "토마토";
        Long categoryId = 1L;
        String categoryName = "과채류";

        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();

        CropType existingCropType = CropType.builder()
                .typeId(typeId)
                .typeName(typeName)
                .category(category)
                .build();

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(typeName)  // 기존과 동일
                .categoryId(categoryId)  // 기존과 동일
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(existingCropType));

        // when
        CropTypeResponse response = cropTypeService.updateCropType(typeId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTypeId()).isEqualTo(typeId);
        assertThat(response.getTypeName()).isEqualTo(typeName);
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo(categoryName);

        verify(cropTypeRepository, times(1)).findById(typeId);
        // 동일한 값이므로 중복 검증 및 카테고리 조회가 호출되지 않음
        verify(cropTypeRepository, never()).existsByTypeNameAndDeletedFalse(anyString());
        verify(cropCategoryRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("작물 수정 - typeName은 동일, categoryId만 변경")
    void updateCropType_sameTypeName_categoryIdChanged() {

        // given
        Long typeId = 1L;
        String typeName = "토마토";

        Long oldCategoryId = 1L;
        String oldCategoryName = "과채류";

        Long newCategoryId = 2L;
        String newCategoryName = "엽채류";

        CropCategory oldCategory = CropCategory.builder()
                .categoryId(oldCategoryId)
                .categoryName(oldCategoryName)
                .build();

        CropCategory newCategory = CropCategory.builder()
                .categoryId(newCategoryId)
                .categoryName(newCategoryName)
                .build();

        CropType existingCropType = CropType.builder()
                .typeId(typeId)
                .typeName(typeName)
                .category(oldCategory)
                .build();

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(typeName)  // 기존과 동일
                .categoryId(newCategoryId)  // 변경
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(existingCropType));
        when(cropCategoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));

        // when
        CropTypeResponse response = cropTypeService.updateCropType(typeId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTypeId()).isEqualTo(typeId);
        assertThat(response.getTypeName()).isEqualTo(typeName);
        assertThat(response.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(response.getCategoryName()).isEqualTo(newCategoryName);

        verify(cropTypeRepository, times(1)).findById(typeId);
        // typeName이 동일하므로 중복 검증은 호출되지 않음
        verify(cropTypeRepository, never()).existsByTypeNameAndDeletedFalse(anyString());
        verify(cropCategoryRepository, times(1)).findById(newCategoryId);
    }

    @Test
    @DisplayName("작물 수정 - categoryId는 동일, typeName만 변경")
    void updateCropType_sameCategoryId_typeNameChanged() {

        // given
        Long typeId = 1L;
        String originalName = "토마토";
        String newName = "방울토마토";

        Long categoryId = 1L;
        String categoryName = "과채류";

        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();

        CropType existingCropType = CropType.builder()
                .typeId(typeId)
                .typeName(originalName)
                .category(category)
                .build();

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(newName)  // 변경
                .categoryId(categoryId)  // 기존과 동일
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(existingCropType));
        when(cropTypeRepository.existsByTypeNameAndDeletedFalse(newName)).thenReturn(false);

        // when
        CropTypeResponse response = cropTypeService.updateCropType(typeId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTypeId()).isEqualTo(typeId);
        assertThat(response.getTypeName()).isEqualTo(newName);
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo(categoryName);

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropTypeRepository, times(1)).existsByTypeNameAndDeletedFalse(newName);
        // categoryId가 동일하므로 카테고리 조회는 호출되지 않음
        verify(cropCategoryRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("작물 수정 실패 - 존재하지 않는 작물")
    void updateCropType_notFoundCropType_ThrowsException() {

        // given
        Long typeId = 999L;

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("새로운이름")
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropTypeService.updateCropType(typeId, request))
                .isInstanceOf(CropTypeNotFoundException.class)
                .hasMessageContaining("작물을 찾을 수 없습니다");

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropTypeRepository, never()).existsByTypeNameAndDeletedFalse(anyString());
        verify(cropCategoryRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("작물 수정 실패 - 중복된 작물명")
    void updateCropType_duplicateTypeName_ThrowsException() {

        // given
        Long typeId = 1L;
        String originalName = "토마토";
        String duplicateName = "오이";  // 이미 다른 작물이 사용 중

        Long categoryId = 1L;
        String categoryName = "과채류";

        CropCategory category = CropCategory.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();

        CropType existingCropType = CropType.builder()
                .typeId(typeId)
                .typeName(originalName)
                .category(category)
                .build();

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(duplicateName)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(existingCropType));
        when(cropTypeRepository.existsByTypeNameAndDeletedFalse(duplicateName)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> cropTypeService.updateCropType(typeId, request))
                .isInstanceOf(DuplicateCropTypeNameException.class)
                .hasMessageContaining("이미 존재하는 작물명입니다");

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropTypeRepository, times(1)).existsByTypeNameAndDeletedFalse(duplicateName);
        verify(cropCategoryRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("작물 수정 실패 - 존재하지 않는 카테고리")
    void updateCropType_notFoundCategory_ThrowsException() {

        // given
        Long typeId = 1L;
        String typeName = "토마토";

        Long oldCategoryId = 1L;
        String oldCategoryName = "과채류";

        Long invalidCategoryId = 999L;

        CropCategory oldCategory = CropCategory.builder()
                .categoryId(oldCategoryId)
                .categoryName(oldCategoryName)
                .build();

        CropType existingCropType = CropType.builder()
                .typeId(typeId)
                .typeName(typeName)
                .category(oldCategory)
                .build();

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .categoryId(invalidCategoryId)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(existingCropType));
        when(cropCategoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cropTypeService.updateCropType(typeId, request))
                .isInstanceOf(CropCategoryNotFoundException.class)
                .hasMessageContaining("작물 카테고리를 찾을 수 없습니다");

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropCategoryRepository, times(1)).findById(invalidCategoryId);
        verify(cropTypeRepository, never()).existsByTypeNameAndDeletedFalse(anyString());
    }

    @Test
    @DisplayName("작물 수정 실패 - 빈 요청 (두 필드 모두 null)")
    void updateCropType_emptyRequest_ThrowsException() {

        // given
        Long typeId = 1L;

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(null)
                .categoryId(null)
                .build();

        // when, then
        assertThatThrownBy(() -> cropTypeService.updateCropType(typeId, request))
                .isInstanceOf(InvalidCropRequestException.class);

        // 빈 요청이므로 어떤 repository 메서드도 호출되지 않음
        verify(cropTypeRepository, never()).findById(anyLong());
        verify(cropTypeRepository, never()).existsByTypeNameAndDeletedFalse(anyString());
        verify(cropCategoryRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("작물 삭제 성공 - 품종이 없는 경우")
    void deleteCropType_Success() {

        // given
        Long typeId = 1L;
        CropCategory category = CropCategory.builder()
                .categoryId(1L)
                .categoryName("과채류")
                .build();

        CropType cropType = CropType.builder()
                .typeId(typeId)
                .typeName("토마토")
                .category(category)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(cropType));
        when(cropVarietyRepository.existsByCropTypeTypeIdAndDeletedFalse(typeId))
                .thenReturn(false);

        // when
        cropTypeService.deleteCropType(typeId);

        // then
        assertThat(cropType.isDeleted()).isTrue();
        assertThat(cropType.getDeletedAt()).isNotNull();

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropVarietyRepository, times(1))
                .existsByCropTypeTypeIdAndDeletedFalse(typeId);
    }

    @Test
    @DisplayName("작물 삭제 실패 - 존재하지 않는 작물")
    void deleteCropType_Fail_NotFound() {

        // given
        Long typeId = 999L;
        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cropTypeService.deleteCropType(typeId))
                .isInstanceOf(CropTypeNotFoundException.class)
                .hasMessageContaining("작물을 찾을 수 없습니다");

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropVarietyRepository, never())
                .existsByCropTypeTypeIdAndDeletedFalse(anyLong());
    }

    @Test
    @DisplayName("작물 삭제 실패 - 연결된 품종이 존재하는 경우")
    void deleteCropType_Fail_HasVarieties() {

        // given
        Long typeId = 1L;
        CropCategory category = CropCategory.builder()
                .categoryId(1L)
                .categoryName("과채류")
                .build();

        CropType cropType = CropType.builder()
                .typeId(typeId)
                .typeName("토마토")
                .category(category)
                .build();

        when(cropTypeRepository.findById(typeId)).thenReturn(Optional.of(cropType));
        when(cropVarietyRepository.existsByCropTypeTypeIdAndDeletedFalse(typeId))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> cropTypeService.deleteCropType(typeId))
                .isInstanceOf(CropTypeHasVarietiesException.class)
                .hasMessageContaining("해당 작물에 연결된 품종이 있어 삭제할 수 없습니다");

        assertThat(cropType.isDeleted()).isFalse();

        verify(cropTypeRepository, times(1)).findById(typeId);
        verify(cropVarietyRepository, times(1))
                .existsByCropTypeTypeIdAndDeletedFalse(typeId);
    }
}