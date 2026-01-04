package com.cropkeeper.crop.controller;

import com.cropkeeper.auth.dto.request.LoginRequest;
import com.cropkeeper.auth.dto.request.RegisterRequest;
import com.cropkeeper.crop.dto.request.CreateCropTypeRequest;
import com.cropkeeper.crop.dto.request.UpdateCropTypeRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private CropVarietyRepository cropVarietyRepository;

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
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 작물 카테고리입니다.")));
    }

    @Test
    @DisplayName("전체 작물 조회 성공")
    void getAllCropTypes_Success() throws Exception {

        // given
        CropCategory category1 = CropCategory.builder()
                .categoryName("테스트카테고리1")
                .build();
        cropCategoryRepository.save(category1);

        CropCategory category2 = CropCategory.builder()
                .categoryName("테스트카테고리2")
                .build();
        cropCategoryRepository.save(category2);

        CropType cropType1 = CropType.builder()
                .typeName("테스트작물1")
                .category(category1)
                .build();
        cropTypeRepository.save(cropType1);

        CropType cropType2 = CropType.builder()
                .typeName("테스트작물2")
                .category(category1)
                .build();
        cropTypeRepository.save(cropType2);

        CropType cropType3 = CropType.builder()
                .typeName("테스트작물3")
                .category(category2)
                .build();
        cropTypeRepository.save(cropType3);

        // when, then
        mockMvc.perform(get("/api/crop-types")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("전체 작물 조회 - 빈 목록")
    void getAllCropTypes_EmptyList() throws Exception {

        // when, then
        mockMvc.perform(get("/api/crop-types")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

    }

    @Test
    @DisplayName("전체 작물 조회 - 삭제된 작물 제외")
    void getAllCropTypes_ExcludeDeletedCrops() throws Exception {

        // given
        // 카테고리
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType1 = CropType.builder()
                .typeName("테스트작물1")
                .category(category)
                .build();
        cropTypeRepository.save(cropType1);

        // 삭제될 작물
        CropType cropType2 = CropType.builder()
                .typeName("삭제될작물")
                .category(category)
                .build();
        cropTypeRepository.save(cropType2);
        // 작물 삭제
        cropType2.delete();
        cropTypeRepository.save(cropType2);

        // when, then
        mockMvc.perform(get("/api/crop-types")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

    }

    @Test
    @DisplayName("카테고리별 작물 조회 성공")
    void getCropTypesByCategory_Success() throws Exception {

        // given
        CropCategory category1 = CropCategory.builder()
                .categoryName("테스트카테고리1")
                .build();
        cropCategoryRepository.save(category1);

        CropCategory category2 = CropCategory.builder()
                .categoryName("테스트카테고리2")
                .build();
        cropCategoryRepository.save(category2);

        // 카테고리1 소속 작물
        CropType cropType1 = CropType.builder()
                .typeName("테스트작물1")
                .category(category1)
                .build();

        // 카테고리1 소속 작물
        CropType cropType2 = CropType.builder()
                .typeName("테스트작물2")
                .category(category1)
                .build();

        // 카테고리2 소속 작물
        CropType cropType3 = CropType.builder()
                .typeName("테스트작물3")
                .category(category2)
                .build();

        cropTypeRepository.save(cropType1);
        cropTypeRepository.save(cropType2);
        cropTypeRepository.save(cropType3);

        // when, then
        mockMvc.perform(get("/api/crop-types")
                        .header("Authorization", "Bearer " + userToken)
                        .param("categoryId", category1.getCategoryId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("카테고리별 작물 조회 성공 - 빈목록")
    void getCropTypesByCategory_EmptyList() throws Exception {

        // given

        // when, then
        mockMvc.perform(get("/api/crop-types")
                        .param("categoryId", testCategoryId.toString())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("카테고리별 작물 조회 성공 - 삭제된 작물")
    void getCropTypesByCategory_ExcludeDeletedCrops() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType1 = CropType.builder()
                .typeName("테스트작물1")
                .category(category)
                .build();
        cropTypeRepository.save(cropType1);

        CropType cropType2 = CropType.builder()
                .typeName("테스트작물2")
                .category(category)
                .build();
        cropTypeRepository.save(cropType2);

        // 삭제할 작물
        CropType cropType3 = CropType.builder()
                .typeName("테스트작물3")
                .category(category)
                .build();
        cropTypeRepository.save(cropType3);
        cropType3.delete();
        cropTypeRepository.save(cropType3);

        // when, then
        mockMvc.perform(get("/api/crop-types")
                        .param("categoryId", testCategoryId.toString())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("작물 ID로 조회 성공")
    void getCropTypeById_Success() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        // when, then
        mockMvc.perform(get("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeId").value(savedCropType.getTypeId()))
                .andExpect(jsonPath("$.typeName").value("테스트작물"))
                .andExpect(jsonPath("$.categoryId").value(testCategoryId))
                .andExpect(jsonPath("$.categoryName").value("테스트카테고리"));

    }

    @Test
    @DisplayName("작물 ID로 조회 실패 - 존재하지 않은 ID")
    void getCropTypeById_Fail_NotFound() throws Exception {

        // given
        Long nonExistentId = 999L;

        // when, then
        mockMvc.perform(get("/api/crop-types/{typeId}", nonExistentId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 작물입니다.")));
    }

    @Test
    @DisplayName("작물 ID로 조회 실패 - 삭제된 작물")
    void getCropTypeById_Fail_DeletedCrop() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        savedCropType.delete();
        cropTypeRepository.save(cropType);

        // when, then
        mockMvc.perform(get("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 작물입니다.")));

    }

    @Test
    @DisplayName("작물 수정 성공 - 작물명만 수정")
    void updateCropType_Success_UpdateTypeNameOnly() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();

        CropType cropType = CropType.builder()
                .typeName("기존작물명")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("수정된작물명")
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeId").value(savedCropType.getTypeId()))
                .andExpect(jsonPath("$.typeName").value("수정된작물명"))
                .andExpect(jsonPath("$.categoryId").value(testCategoryId))
                .andExpect(jsonPath("$.categoryName").value("테스트카테고리"));
    }

    @Test
    @DisplayName("작물 수정 성공 - 카테고리만 수정")
    void updateCropType_Success_UpdateCategoryOnly() throws Exception {

        // given
        // 새로운 카테고리 생성
        CropCategory newCategory = CropCategory.builder()
                .categoryName("새카테고리")
                .build();
        CropCategory savedNewCategory = cropCategoryRepository.save(newCategory);

        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(null)
                .categoryId(savedNewCategory.getCategoryId())
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeId").value(savedCropType.getTypeId()))
                .andExpect(jsonPath("$.typeName").value("테스트작물"))
                .andExpect(jsonPath("$.categoryId").value(savedNewCategory.getCategoryId()))
                .andExpect(jsonPath("$.categoryName").value("새카테고리"));
    }

    @Test
    @DisplayName("작물 수정 성공 - 작물명과 카테고리 모두 수정")
    void updateCropType_Success_UpdateBoth() throws Exception {

        // given
        CropCategory newCategory = CropCategory.builder()
                .categoryName("새카테고리")
                .build();
        CropCategory savedNewCategory = cropCategoryRepository.save(newCategory);

        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("기존작물명")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("새작물명")
                .categoryId(savedNewCategory.getCategoryId())
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeId").value(savedCropType.getTypeId()))
                .andExpect(jsonPath("$.typeName").value("새작물명"))
                .andExpect(jsonPath("$.categoryId").value(savedNewCategory.getCategoryId()))
                .andExpect(jsonPath("$.categoryName").value("새카테고리"));
    }

    @Test
    @DisplayName("작물 수정 성공 - 동일한 값으로 수정")
    void updateCropType_Success_UpdateWithSameValue() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("테스트작물") // 동일한 이름
                .categoryId(testCategoryId) // 동일한 카테고리
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeId").value(savedCropType.getTypeId()))
                .andExpect(jsonPath("$.typeName").value("테스트작물"))
                .andExpect(jsonPath("$.categoryId").value(testCategoryId))
                .andExpect(jsonPath("$.categoryName").value("테스트카테고리"));
    }

    @Test
    @DisplayName("작물 수정 성공 - 작물명 최대 길이로 수정")
    void updateCropType_Success_MaxLengthTypeName() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("기존작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        String maxLengthName = "1".repeat(20);
        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(maxLengthName)
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeName").value(maxLengthName));
    }

    @Test
    @DisplayName("작물 수정 성공 - 빈 문자열로 작물명 수정 시도 (무시됨)")
    void updateCropType_Success_BlankTypeNameIgnored() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("기존작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("") // 빈 문자열 (무시됨)
                .categoryId(null)
                .build();

        // when, then
        // 빈 문자열은 무시되고, 변경사항 없이 200 OK 반환
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeName").value("기존작물")) // 변경되지 않음
                .andExpect(jsonPath("$.categoryId").value(testCategoryId));
    }

    @Test
    @DisplayName("작물 수정 실패 - null 필드만 포함한 요청")
    void updateCropType_Fail_AllFieldsNull() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(null)
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("잘못된 작물 요청입니다")));
    }

    @Test
    @DisplayName("작물 수정 실패 - 존재하지 않는 작물 ID")
    void updateCropType_Fail_CropTypeNotFound() throws Exception {

        // given
        Long nonExistentId = 9999L;
        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("수정된작물명")
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", nonExistentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 작물입니다.")));
    }

    @Test
    @DisplayName("작물 수정 실패 - 삭제된 작물 수정 시도")
    void updateCropType_Fail_DeletedCropType() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        // 작물 삭제
        savedCropType.delete();
        cropTypeRepository.save(savedCropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("수정된작물명")
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 작물입니다.")));
    }

    @Test
    @DisplayName("작물 수정 실패 - 중복된 작물명으로 수정")
    void updateCropType_Fail_DuplicateTypeName() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();

        // 기존 작물 1
        CropType existingCropType = CropType.builder()
                .typeName("중복될작물명")
                .category(category)
                .build();
        cropTypeRepository.save(existingCropType);

        // 수정할 작물 2
        CropType cropTypeToUpdate = CropType.builder()
                .typeName("수정전작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropTypeToUpdate);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("중복될작물명") // 이미 존재하는 이름
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("이미 존재하는 작물명입니다")));
    }

    @Test
    @DisplayName("작물 수정 실패 - 존재하지 않는 카테고리로 수정")
    void updateCropType_Fail_CategoryNotFound() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName(null)
                .categoryId(9999L) // 존재하지 않는 카테고리 ID
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 작물 카테고리입니다.")));
    }

    @Test
    @DisplayName("작물 수정 실패 - 작물명 길이 초과")
    void updateCropType_Fail_TypeNameTooLong() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("1".repeat(21)) // 21자 (최대 20자 초과)
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.typeName").value("작물명은 20자 이하여야 합니다."));
    }

    @Test
    @DisplayName("작물 수정 실패 - 일반 사용자 권한")
    void updateCropType_Fail_UserRole() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("수정된작물명")
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + userToken) // USER 권한
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("작물 수정 실패 - 인증되지 않은 요청")
    void updateCropType_Fail_NoAuth() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("테스트작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        UpdateCropTypeRequest request = UpdateCropTypeRequest.builder()
                .typeName("수정된작물명")
                .categoryId(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        // Authorization 헤더 없음
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("작물 삭제 성공 - ADMIN이 품종이 없는 작물 삭제")
    void deleteCropType_Success() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("삭제할작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        // when, then
        mockMvc.perform(delete("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("작물 삭제 실패 - 품종이 연결된 작물 삭제 시도")
    void deleteCropType_Fail_HasVarieties() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("품종있는작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        // 품종 생성
        CropVariety variety = CropVariety.builder()
                .cropType(savedCropType)
                .varietyName("테스트품종")
                .build();
        cropVarietyRepository.save(variety);

        // when, then
        mockMvc.perform(delete("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("해당 작물에 연결된 품종이 있어 삭제할 수 없습니다")));
    }

    @Test
    @DisplayName("작물 삭제 실패 - 존재하지 않는 작물 ID")
    void deleteCropType_Fail_NotFound() throws Exception {

        // given
        Long nonExistentId = 9999L;

        // when, then
        mockMvc.perform(delete("/api/crop-types/{typeId}", nonExistentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 작물입니다.")));
    }

    @Test
    @DisplayName("작물 삭제 실패 - 일반 사용자 권한")
    void deleteCropType_Fail_UserRole() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("삭제할작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        // when, then
        mockMvc.perform(delete("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        .header("Authorization", "Bearer " + userToken) // USER 권한
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("작물 삭제 실패 - 인증되지 않은 요청")
    void deleteCropType_Fail_NoAuth() throws Exception {

        // given
        CropCategory category = cropCategoryRepository.findById(testCategoryId).get();
        CropType cropType = CropType.builder()
                .typeName("삭제할작물")
                .category(category)
                .build();
        CropType savedCropType = cropTypeRepository.save(cropType);

        // when, then
        mockMvc.perform(delete("/api/crop-types/{typeId}", savedCropType.getTypeId())
                        // Authorization 헤더 없음
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
