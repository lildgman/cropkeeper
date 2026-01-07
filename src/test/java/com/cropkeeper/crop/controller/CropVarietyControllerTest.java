package com.cropkeeper.crop.controller;

import com.cropkeeper.auth.dto.request.LoginRequest;
import com.cropkeeper.crop.dto.request.CreateCropVarietyRequest;
import com.cropkeeper.crop.entity.CropCategory;
import com.cropkeeper.crop.entity.CropType;
import com.cropkeeper.crop.entity.CropVariety;
import com.cropkeeper.crop.repository.CropCategoryRepository;
import com.cropkeeper.crop.repository.CropTypeRepository;
import com.cropkeeper.crop.repository.CropVarietyRepository;
import com.cropkeeper.member.entity.Member;
import com.cropkeeper.member.entity.MemberRole;
import com.cropkeeper.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CropVarietyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CropCategoryRepository cropCategoryRepository;

    @Autowired
    CropTypeRepository cropTypeRepository;

    @Autowired
    CropVarietyRepository cropVarietyRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private Long testCategoryId;
    private Long testTypeId;


    @BeforeEach
    void setUp() throws Exception {

        cropCategoryRepository.deleteAll();
        cropTypeRepository.deleteAll();
        cropVarietyRepository.deleteAll();
        memberRepository.deleteAll();

        // 유저 생성
        Member member = Member.builder()
                .username("testuser")
                .password(passwordEncoder.encode("Password123!"))
                .name("testuser")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();
        memberRepository.save(member);

        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("Password123!")
                .build();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        userToken = jsonNode.get("accessToken").asText();

        CropCategory category = CropCategory.builder()
                .categoryName("테스트카테고리")
                .build();

        CropCategory savedCategory = cropCategoryRepository.save(category);
        testCategoryId = savedCategory.getCategoryId();

        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(savedCategory)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);
        testTypeId = savedCropType.getTypeId();
    }

    @Test
    @DisplayName("품종 생성 성공")
    void createCropVariety_Success() throws Exception {

        // given
        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .varietyName("테스트품종")
                .typeId(testTypeId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.varietyId").exists())
                .andExpect(jsonPath("$.varietyName").value("테스트품종"))
                .andExpect(jsonPath("$.typeId").value(testTypeId))
                .andExpect(jsonPath("$.typeName").value("테스트작물"))
                .andExpect(jsonPath("$.categoryId").value(testCategoryId))
                .andExpect(jsonPath("$.categoryName").value("테스트카테고리"));

    }

    @Test
    @DisplayName("품종 생성 실패 - typeId 가 null")
    void createCropVariety_Fail_TypeIdIsNull() throws Exception {

        // given
        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .varietyName("테스트품종")
                .typeId(null)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.typeId").value(containsString("카테고리 ID는 필수입니다")));
    }

    @Test
    @DisplayName("품종 생성 실패 - 품종명 공백")
    void createCropVariety_Fail_VarietyNameBlank() throws Exception {

        // given
        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .varietyName("")
                .typeId(testTypeId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.varietyName").value("품종명은 필수입니다."));
    }

    @Test
    @DisplayName("품종 생성 실패 - 품종명 길이 초과(20자 이상)")
    void createCropVariety_Fail_VarietyNameTooLong() throws Exception {

        // given
        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .varietyName("a".repeat(21))
                .typeId(testTypeId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.varietyName").value("품종명은 20자 이하여야 합니다."));
    }

    @Test
    @DisplayName("품종 생성 실패 - 없는 CropType")
    void createCropVariety_Fail_CropTypeNotFound() throws Exception {

        // given
        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .varietyName("테스트품종")
                .typeId(9999L)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 작물입니다.")));

    }

    @Test
    @DisplayName("품종 생성 실패 - 품종명 중복")
    void createCropVariety_Fail_DuplicateVarietyName() throws Exception {

        // given
        // 품종 등록
        CropType cropType = cropTypeRepository.findById(testTypeId).get();

        CropVariety existingCropVariety = CropVariety.builder()
                .varietyName("테스트품종")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(existingCropVariety);

        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .varietyName("테스트품종")
                .typeId(testTypeId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("이미 존재하는 품종명입니다.")));

    }

    @Test
    @DisplayName("품종 생성 실패 - 인증토큰없음")
    void createCropVariety_Fail_NoAuth() throws Exception {

        // given
        CreateCropVarietyRequest request = CreateCropVarietyRequest.builder()
                .varietyName("테스트품종")
                .typeId(testTypeId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-varieties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ============== getAllVarieties() 메서드 테스트 ==============

    @Test
    @DisplayName("전체 품종 조회 성공 - typeId 파라미터 없음")
    void getAllVarieties_Success_WithoutTypeId() throws Exception {

        // given
        CropType cropType = cropTypeRepository.findById(testTypeId).get();

        CropVariety variety1 = CropVariety.builder()
                .varietyName("품종1")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(variety1);

        CropVariety variety2 = CropVariety.builder()
                .varietyName("품종2")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(variety2);

        // when, then
        mockMvc.perform(get("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].varietyName").value("품종1"))
                .andExpect(jsonPath("$[1].varietyName").value("품종2"));
    }

    @Test
    @DisplayName("전체 품종 조회 성공 - 여러 개의 품종 존재")
    void getAllVarieties_Success_MultipleVarieties() throws Exception {

        // given
        // 카테고리 2개 생성
        CropCategory category1 = CropCategory.builder()
                .categoryName("카테고리1")
                .build();
        cropCategoryRepository.save(category1);

        CropCategory category2 = CropCategory.builder()
                .categoryName("카테고리2")
                .build();
        cropCategoryRepository.save(category2);

        // 작물 2개 생성
        CropType type1 = CropType.builder()
                .typeName("작물1")
                .category(category1)
                .build();
        cropTypeRepository.save(type1);

        CropType type2 = CropType.builder()
                .typeName("작물2")
                .category(category2)
                .build();
        cropTypeRepository.save(type2);

        // 각 작물에 품종 생성
        CropVariety variety1 = CropVariety.builder()
                .varietyName("작물1-품종1")
                .cropType(type1)
                .build();
        cropVarietyRepository.save(variety1);

        CropVariety variety2 = CropVariety.builder()
                .varietyName("작물1-품종2")
                .cropType(type1)
                .build();
        cropVarietyRepository.save(variety2);

        CropVariety variety3 = CropVariety.builder()
                .varietyName("작물2-품종1")
                .cropType(type2)
                .build();
        cropVarietyRepository.save(variety3);

        // when, then
        mockMvc.perform(get("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("특정 작물의 품종 조회 성공 - typeId 파라미터 있음")
    void getAllVarieties_Success_WithTypeId() throws Exception {

        // given
        CropType cropType = cropTypeRepository.findById(testTypeId).get();

        CropVariety variety1 = CropVariety.builder()
                .varietyName("토마토품종1")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(variety1);

        CropVariety variety2 = CropVariety.builder()
                .varietyName("토마토품종2")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(variety2);

        // when, then
        mockMvc.perform(get("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .param("typeId", testTypeId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].varietyName").value("토마토품종1"))
                .andExpect(jsonPath("$[1].varietyName").value("토마토품종2"))
                .andExpect(jsonPath("$[0].typeId").value(testTypeId))
                .andExpect(jsonPath("$[1].typeId").value(testTypeId));
    }

    @Test
    @DisplayName("전체 품종 조회 - 빈 목록 (품종이 하나도 없음)")
    void getAllVarieties_EmptyList_NoVarieties() throws Exception {

        // given
        // 품종을 생성하지 않음

        // when, then
        mockMvc.perform(get("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("특정 작물의 품종 조회 - 빈 목록 (해당 작물에 품종 없음)")
    void getAllVarieties_EmptyList_NoVarietiesForType() throws Exception {

        // given
        // testTypeId에 품종을 생성하지 않음

        // when, then
        mockMvc.perform(get("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .param("typeId", testTypeId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("전체 품종 조회 - 삭제된 품종 제외")
    void getAllVarieties_ExcludeDeleted_AllVarieties() throws Exception {

        // given
        CropType cropType = cropTypeRepository.findById(testTypeId).get();

        // 활성 품종
        CropVariety activeVariety = CropVariety.builder()
                .varietyName("활성품종")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(activeVariety);

        // 삭제된 품종
        CropVariety deletedVariety = CropVariety.builder()
                .varietyName("삭제된품종")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(deletedVariety);
        deletedVariety.delete();
        cropVarietyRepository.save(deletedVariety);

        // when, then
        mockMvc.perform(get("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].varietyName").value("활성품종"));
    }

    @Test
    @DisplayName("특정 작물의 품종 조회 - 삭제된 품종 제외")
    void getAllVarieties_ExcludeDeleted_ByTypeId() throws Exception {

        // given
        CropType cropType = cropTypeRepository.findById(testTypeId).get();

        // 활성 품종 2개
        CropVariety activeVariety1 = CropVariety.builder()
                .varietyName("활성품종1")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(activeVariety1);

        CropVariety activeVariety2 = CropVariety.builder()
                .varietyName("활성품종2")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(activeVariety2);

        // 삭제된 품종
        CropVariety deletedVariety = CropVariety.builder()
                .varietyName("삭제된품종")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(deletedVariety);
        deletedVariety.delete();
        cropVarietyRepository.save(deletedVariety);

        // when, then
        mockMvc.perform(get("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .param("typeId", testTypeId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].varietyName").value("활성품종1"))
                .andExpect(jsonPath("$[1].varietyName").value("활성품종2"));
    }

    @Test
    @DisplayName("특정 작물의 품종 조회 실패 - 존재하지 않는 typeId")
    void getAllVarieties_Fail_TypeIdNotFound() throws Exception {

        // given
        Long nonExistentTypeId = 9999L;

        // when, then
        mockMvc.perform(get("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .param("typeId", nonExistentTypeId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 작물입니다.")));
    }

    @Test
    @DisplayName("품종 조회 실패 - 인증 토큰 없음")
    void getAllVarieties_Fail_NoAuth() throws Exception {

        // given
        // 품종 생성
        CropType cropType = cropTypeRepository.findById(testTypeId).get();
        CropVariety variety = CropVariety.builder()
                .varietyName("테스트품종")
                .cropType(cropType)
                .build();
        cropVarietyRepository.save(variety);

        // when, then
        mockMvc.perform(get("/api/crop-varieties")
                        // Authorization 헤더 없음
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("여러 작물의 품종 조회 - typeId로 필터링 확인")
    void getAllVarieties_Success_FilterByTypeId() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();

        // 작물 A (기존 testTypeId 사용)
        CropType cropTypeA = cropTypeRepository.findById(testTypeId).get();

        // 작물 B 생성
        CropType cropTypeB = CropType.builder()
                .typeName("작물B")
                .category(category)
                .build();
        cropTypeRepository.save(cropTypeB);

        // 작물 A의 품종
        CropVariety varietyA1 = CropVariety.builder()
                .varietyName("작물A-품종1")
                .cropType(cropTypeA)
                .build();
        cropVarietyRepository.save(varietyA1);

        CropVariety varietyA2 = CropVariety.builder()
                .varietyName("작물A-품종2")
                .cropType(cropTypeA)
                .build();
        cropVarietyRepository.save(varietyA2);

        // 작물 B의 품종
        CropVariety varietyB1 = CropVariety.builder()
                .varietyName("작물B-품종1")
                .cropType(cropTypeB)
                .build();
        cropVarietyRepository.save(varietyB1);

        // when, then - 작물 A의 품종만 조회
        mockMvc.perform(get("/api/crop-varieties")
                        .header("Authorization", "Bearer " + userToken)
                        .param("typeId", testTypeId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].varietyName").value("작물A-품종1"))
                .andExpect(jsonPath("$[1].varietyName").value("작물A-품종2"))
                .andExpect(jsonPath("$[0].typeId").value(testTypeId))
                .andExpect(jsonPath("$[1].typeId").value(testTypeId));
    }
}
