package com.cropkeeper.domain.crop.controller;

import com.cropkeeper.domain.auth.dto.request.LoginRequest;
import com.cropkeeper.domain.auth.dto.request.RegisterRequest;
import com.cropkeeper.domain.crop.dto.request.CreateCropCategoryRequest;
import com.cropkeeper.domain.crop.dto.request.UpdateCropCategoryRequest;
import com.cropkeeper.domain.crop.entity.CropCategory;
import com.cropkeeper.domain.crop.repository.CropCategoryRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CropCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

        cropCategoryRepository.deleteAll();
        memberRepository.deleteAll();

        // 관리자 생성
        Member admin = Member.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin123!"))
                .name("관리자")
                .contact("01012345678")
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

        // 일반 사용자 생성
        RegisterRequest userRegisterRequest = RegisterRequest.builder()
                .username("testuser")
                .password("User123!")
                .passwordConfirm("User123!")
                .name("Test")
                .contact("01012345678")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterRequest)))
                .andExpect(status().isCreated());

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

        // 테스트 카테고리 생성
        CropCategory category = CropCategory.builder()
                .categoryName("과일류")
                .build();
        CropCategory savedCategory = cropCategoryRepository.save(category);
        testCategoryId = savedCategory.getCategoryId();

    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_Successs() throws Exception {

        // given
        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName("채소류")
                .build();

        // when, then
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").exists())
                .andExpect(jsonPath("$.categoryName").value("채소류"))
                .andExpect(jsonPath("$.createdAt").exists());

    }

    @Test
    @DisplayName("전체 카테고리 목록 조회 성공")
    void getCategories_Success() throws Exception{

        CropCategory category2 = CropCategory.builder()
                .categoryName("채소류")
                .build();
        cropCategoryRepository.save(category2);

        CropCategory category3 = CropCategory.builder()
                .categoryName("곡물류")
                .build();
        cropCategoryRepository.save(category3);

        // when, then
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].categoryName", containsInAnyOrder("과일류", "채소류", "곡물류")));

    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() throws Exception {

        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName("열대과일류")
                .build();

        // when, then
        mockMvc.perform(put("/api/categories/{categoryId}", testCategoryId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(testCategoryId))
                .andExpect(jsonPath("$.categoryName").value("열대과일류"))
                .andExpect(jsonPath("$.createdAt").exists());

    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() throws Exception {

        // when, then
        mockMvc.perform(delete("/api/categories/{categoryId}", testCategoryId)
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    // 경계조건

    @Test
    @DisplayName("빈 카테고리 목록 조회")
    void getCategoris_EmptyList() throws Exception {

        // given
        cropCategoryRepository.deleteAll();

        // when, then
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("카테고리명을 동일한 이름으로 수정 -> 변경 x")
    void updateCategory_SameName() throws Exception {

        // given
        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName("과일류")
                .build();

        // when, then
        mockMvc.perform(put("/api/categories/{categoryId}", testCategoryId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("과일류"));

    }

    @Test
    @DisplayName("카테고리명의 길이가 최대길이")
    void createCategory_MaxLengthName() throws Exception {

        // given
        String maxLengthName = "a".repeat(10);
        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName(maxLengthName)
                .build();

        // when, then
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value(maxLengthName));
    }

    // 권한 관련
    @Test
    @DisplayName("인증되지 않은 사용자의 카테고리 생성 시도")
    void createCategory_NoAuth() throws Exception {

        // given
        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName("채소류")
                .build();

        // when, then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("일반 사용자의 카테고리 생성 시도")
    void createCategory_Fail_UserRole() throws Exception {

        // given
        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName("채소류")
                .build();

        // when, then
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("일반 사용자의 카테고리 수정 시도")
    void updateCategory_Fail_UserRole() throws Exception {

        // given
        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName("이름수정")
                .build();

        // when, then
        mockMvc.perform(put("/api/categories/{categoryId}", testCategoryId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("일반 사용자의 카테고리 삭제 시도")
    void deleteCategory_Fail_UserRole() throws Exception {

        // when, then
        mockMvc.perform(delete("/api/categories/{categoryId}", testCategoryId)
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("인증되지 않은 사용자의 카테고리 조회 시도")
    void getCategories_Fail_NoAuth() throws Exception {

        // when, then
        mockMvc.perform(get("/api/categories"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // Validation 실패

    @Test
    @DisplayName("카테고리명 누락")
    void createCategory_Fail_NullName() throws Exception {

        // given
        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName(null)
                .build();

        // when, then
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.categoryName").value("카테고리명은 필수입니다."));
    }

    @Test
    @DisplayName("카테고리명 공백")
    void createCategory_Fail_BlankName() throws Exception {

        // given
        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName("")
                .build();

        // when, then
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.categoryName").value("카테고리명은 필수입니다."));

    }

    @Test
    @DisplayName("카테고리명 길이 초과")
    void createCategory_Fail_TooLongName() throws Exception {

        // given
        String tooLong = "a".repeat(11);
        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName(tooLong)
                .build();

        // when, then
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.categoryName").value("카테고리명은 10자 이하여야 합니다."));
    }

    @Test
    @DisplayName("카테고리 수정 시 공백 불가")
    void updateCategory_Fail_BlankName() throws Exception {

        // given
        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName("")
                .build();

        // when, then
        mockMvc.perform(put("/api/categories/{categoryId}", testCategoryId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.categoryName").value("카테고리명은 필수입니다."));

    }

    @Test
    @DisplayName("중복 카테고리명으로 생성")
    void createCategory_Fail_DuplicateName() throws Exception {

        // given
        CreateCropCategoryRequest request = CreateCropCategoryRequest.builder()
                .categoryName("과일류")
                .build();

        // when, then
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("이미 존재하는 카테고리명입니다")));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리ID로 조회")
    void getCategoryById_Fail_NotFound() throws Exception {

        // given
        Long nonExistCategoryId = 9999L;

        // when, then
        mockMvc.perform(get("/api/categories/{categoryId}", nonExistCategoryId)
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("작물 카테고리를 찾을 수 없습니다")));
    }

    @Test
    @DisplayName("존재하지 않은 카테고리 이름으로 조회")
    void getCategoryByName_Fail_NotFound() throws Exception {

        // given
        String nonExistCategoryName = "없는_카테고리_이름";

        // when, then
        mockMvc.perform(get("/api/categories/name/{categoryName}", nonExistCategoryName)
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("작물 카테고리를 찾을 수 없습니다")));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정")
    void updateCategory_Fail_NotFound() throws Exception {

        // given
        Long nonExistCategoryId = 9999L;
        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName("수정할이름")
                .build();

        // when, then
        mockMvc.perform(put("/api/categories/{categoryId}", nonExistCategoryId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("작물 카테고리를 찾을 수 없습니다")));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 삭제")
    void deleteCategory_Fail_NotFound() throws Exception {

        // given
        Long nonExistCategoryId = 9999L;

        // when, then
        mockMvc.perform(delete("/api/categories/{categoryId}", nonExistCategoryId)
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("작물 카테고리를 찾을 수 없습니다")));
    }

    @Test
    @DisplayName("존재하는 카테고리명으로 수정")
    void updateCategory_Fail_DuplicateName() throws Exception {

        // given
        CropCategory vegetable = CropCategory.builder()
                .categoryName("채소류")
                .build();
        cropCategoryRepository.save(vegetable);

        UpdateCropCategoryRequest request = UpdateCropCategoryRequest.builder()
                .categoryName("채소류")
                .build();

        // when, then
        mockMvc.perform(put("/api/categories/{categoryId}", testCategoryId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("이미 존재하는 카테고리명입니다")));
    }
}