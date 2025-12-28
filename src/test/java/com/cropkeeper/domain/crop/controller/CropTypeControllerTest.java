package com.cropkeeper.domain.crop.controller;

import com.cropkeeper.domain.auth.dto.request.LoginRequest;
import com.cropkeeper.domain.auth.dto.request.RegisterRequest;
import com.cropkeeper.domain.crop.dto.request.CreateCropTypeRequest;
import com.cropkeeper.domain.crop.entity.CropCategory;
import com.cropkeeper.domain.crop.entity.CropType;
import com.cropkeeper.domain.crop.repository.CropCategoryRepository;
import com.cropkeeper.domain.crop.repository.CropTypeRepository;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.entity.MemberRole;
import com.cropkeeper.domain.member.repository.MemberRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CropTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CropTypeRepository cropTypeRepository;

    @Autowired
    private CropCategoryRepository cropCategoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Long testCategoryId;

    @BeforeEach
    void setUp() throws Exception {

        cropTypeRepository.deleteAll();
        cropCategoryRepository.deleteAll();
        memberRepository.deleteAll();

        // 관리자 생성
        Member admin = Member.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin123!"))
                .name("admin")
                .contact("01011112222")
                .role(MemberRole.ADMIN)
                .build();
        memberRepository.save(admin);

        LoginRequest adminLoginRequest = LoginRequest.builder()
                .username("admin")
                .password("Admin123!")
                .build();

        String adminLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode adminJsonNode = objectMapper.readTree(adminLoginResponse);
        adminToken = adminJsonNode.get("accessToken").asText();

        RegisterRequest userRegisterRequest = RegisterRequest.builder()
                .username("testuser")
                .password("User123!")
                .passwordConfirm("User123!")
                .name("test")
                .contact("01012345678")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterRequest)))
                .andExpect(status().isCreated());

        // 일반 사용자 생성
        LoginRequest userLoginRequest = LoginRequest.builder()
                .username("testuser")
                .password("User123!")
                .build();

        String userLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode userJsonNode = objectMapper.readTree(userLoginResponse);
        userToken = userJsonNode.get("accessToken").asText();

        // 테스트용 카테고리 생성
        CropCategory category = CropCategory.builder()
                .categoryName("테스트카테고리")
                .build();
        CropCategory savedCategory = cropCategoryRepository.save(category);
        testCategoryId = savedCategory.getCategoryId();

    }

    @Test
    @DisplayName("작물 생성 성공 - 관리자")
    void createCropType_Success() throws Exception {

        // given
        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName("토마토")
                .categoryId(testCategoryId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeId").exists())
                .andExpect(jsonPath("$.typeName").value("토마토"))
                .andExpect(jsonPath("$.categoryId").value(testCategoryId))
                .andExpect(jsonPath("$.categoryName").value("테스트카테고리"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("작물 생성 성공 - 작물명 최대길이")
    void createCropType_Success_MaxLengthTypeName() throws Exception {

        // given
        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName("1".repeat(20))
                .categoryId(testCategoryId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeName").value("1".repeat(20)));

    }

    @Test
    @DisplayName("작물 생성 실패 - 인증되지 않은 사용자")
    void createCropType_Fail_NoAuth() throws Exception {

        // given
        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName("테스트작물")
                .categoryId(testCategoryId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("작물 생성 실패 - 일반 사용자")
    void createCropType_Fail_UserRole() throws Exception {

        // given
        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName("테스트작물")
                .categoryId(testCategoryId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("작물 생성 실패 - 작물명 공백")
    void createCropType_Fail_BlankTypeName() throws Exception {

        // given
        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName("")
                .categoryId(testCategoryId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.typeName").value("작물명은 필수입니다."));
    }

    @Test
    @DisplayName("작물 생성 실패 - 작물명 null")
    void createCropType_Fail_NullTypeName() throws Exception {

        // given
        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName(null)
                .categoryId(testCategoryId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.typeName").value("작물명은 필수입니다."));

    }

    @Test
    @DisplayName("작물 생성 실패 - 작물명 길이 초과")
    void createCropType_Fail_TooLongTypeName() throws Exception {

        // given
        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName("1".repeat(21))
                .categoryId(testCategoryId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.typeName").value("작물명은 20자 이하여야 합니다."));

    }

    @Test
    @DisplayName("작물 생성 실패 - 카테고리 Id null")
    void createCropType_Fail_NullCategoryId() throws Exception {

        // given
        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName("테스트작물")
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.categoryId").value("카테고리 ID는 필수입니다."));

    }

    @Test
    @DisplayName("작물 생성 실패 - 중복된 작물명")
    void createCropType_Fail_DuplicateTypeName() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType existingCropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        cropTypeRepository.save(existingCropType);

        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName("테스트작물")
                .categoryId(testCategoryId)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("이미 존재하는 작물명입니다")));
    }

    @Test
    @DisplayName("작물 생성 실패 - 존재하지 않는 카테고리")
    void createCropType_Fail_CategoryNotFound() throws Exception {

        // given
        CreateCropTypeRequest request = CreateCropTypeRequest.builder()
                .typeName("테스트작물")
                .categoryId(9999L)
                .build();

        // when, then
        mockMvc.perform(post("/api/crop-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("작물 카테고리를 찾을 수 없습니다")));
    }



}
