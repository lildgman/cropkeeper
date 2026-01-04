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
}
