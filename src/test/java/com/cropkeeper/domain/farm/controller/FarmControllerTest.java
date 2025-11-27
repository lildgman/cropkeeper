package com.cropkeeper.domain.farm.controller;

import com.cropkeeper.domain.auth.dto.request.LoginRequest;
import com.cropkeeper.domain.auth.dto.request.RegisterRequest;
import com.cropkeeper.domain.farm.dto.request.CreateFarmRequest;
import com.cropkeeper.domain.farm.dto.request.UpdateFarmRequest;
import com.cropkeeper.domain.farm.repository.FarmRepository;
import com.cropkeeper.domain.member.entity.Member;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FarmRepository farmRepository;

    private Long testMemberId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {

        // db 정리
        farmRepository.deleteAll();
        memberRepository.deleteAll();

        // 테스트용 회원 생성
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser01")
                .password("Pass123!")
                .passwordConfirm("Pass123!")
                .name("test01")
                .contact("01012345678")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        Member member = memberRepository.findByUsername("testuser01").orElseThrow();
        testMemberId = member.getMemberId();

        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser01")
                .password("Pass123!")
                .build();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        accessToken = jsonNode.get("accessToken").asText();

    }

    @Test
    @DisplayName("농장 생성 성공")
    void createFarm_Success() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("테스트농장")
                .zipCode("12345")
                .street("여기가 어디냐면...")
                .detail("여기야")
                .farmSize(2350L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.farmId").exists())
                .andExpect(jsonPath("$.farmName").value("테스트농장"))
                .andExpect(jsonPath("$.zipCode").value("12345"))
                .andExpect(jsonPath("$.street").value("여기가 어디냐면..."))
                .andExpect(jsonPath("$.detail").value("여기야"))
                .andExpect(jsonPath("$.farmSize").value(2350L))
                .andExpect(jsonPath("$.memberId").value(testMemberId))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

    }

    @Test
    @DisplayName("농장 생성 성공 - 필수 필드만")
    void createFarm_Success_RequiredField() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("테스트농장")
                .street("여기가 어디냐면...")
                .farmSize(2350L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.farmName").value("테스트농장"))
                .andExpect(jsonPath("$.zipCode").isEmpty())
                .andExpect(jsonPath("$.street").value("여기가 어디냐면..."))
                .andExpect(jsonPath("$.detail").isEmpty())
                .andExpect(jsonPath("$.farmSize").value(2350L));
    }

    @Test
    @DisplayName("농장 생성 실패 - 인증 토큰 x")
    void createFarm_Fail_NoToken() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("테스트농장")
                .street("여기가 어디냐면...")
                .farmSize(2350L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("농장 생성 실패 - 농장 이름 누락")
    void createFarm_Fail_MissingFarmName() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .street("여기가 어디여")
                .farmSize(1000L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.farmName").value(containsString("필수입니다")));

    }

    @Test
    @DisplayName("농장 생성 실패 - 농장 이름 빈 문자열")
    void createFarm_Fail_EmptyFarmName() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("")
                .street("여기가 어디여")
                .farmSize(1000L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.farmName").exists());

    }

    @Test
    @DisplayName("농장 생성 실패 - 주소 누락")
    void createFarm_Fail_MissingStreet() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("주소누락농장")
                .farmSize(1000L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.street").value(containsString("필수입니다")));

    }

    @Test
    @DisplayName("농장 생성 실패 - 농장 크기 누락")
    void createFarm_Fail_MissingFarmSize() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("크기누락농장")
                .street("여기가 어딘교")
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.farmSize").value(containsString("필수입니다")));

    }

    @Test
    @DisplayName("농장 생성 실패 - 농장 크기 0")
    void createFarm_Fail_FarmSize_0() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("크기0농장")
                .street("ㅇㄷ삼")
                .farmSize(0L)
                .build();

        // when, then
        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.farmSize").value(containsString("1 이상")));

    }

    @Test
    @DisplayName("농장 생성 실패 - 농장 크기 마이너스")
    void createFarm_Fail_FarmSize_Minus() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("크기0농장")
                .street("ㅇㄷ삼")
                .farmSize(-100L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.farmSize").value(containsString("1 이상")));

    }

    @Test
    @DisplayName("농장 생성 실패 - 농장 이름 길이 초과")
    void createFarm_Fail_FarmNameTooLong() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("ㅇ".repeat(101))
                .street("야야야")
                .farmSize(1000L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.farmName").value(containsString("20자 이하")));
    }

    @Test
    @DisplayName("농장 생성 실패 - 주소 길이 초과")
    void createFarm_Fail_StreetTooLong() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("나여농장")
                .street("야".repeat(200))
                .farmSize(1000L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.street").value(containsString("100자 이하")));
    }

    @Test
    @DisplayName("농장 생성 실패 - 상세주소 길이 초과")
    void createFarm_Fail_DetailTooLong() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("여기농장")
                .street("어디냐면")
                .detail("ㅇ".repeat(200))
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.detail").value(containsString("100자 이하")));

    }

    @Test
    @DisplayName("농장 생성 실패 - 우편번호 길이 초과")
    void createFarm_Fail_ZipCodeTooLong() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("여기농장")
                .zipCode("123123123123123123")
                .street("어디냐면")
                .detail("여기거덩")
                .farmSize(1000L)
                .build();

        // when, then
        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.zipCode").value(containsString("10자 이하")));

    }

    @Test
    @DisplayName("내 농장 목록 조회 성공 - 여러개")
    void getMyFarm_Success_MultipleFarms() throws Exception {

        // given
        CreateFarmRequest farmRequest1 = CreateFarmRequest.builder()
                .farmName("농장1")
                .street("주소1")
                .farmSize(1000L)
                .build();

        CreateFarmRequest farmRequest2 = CreateFarmRequest.builder()
                .farmName("농장2")
                .street("주소2")
                .farmSize(2000L)
                .build();

        CreateFarmRequest farmRequest3 = CreateFarmRequest.builder()
                .farmName("농장3")
                .street("주소3")
                .farmSize(3000L)
                .build();

        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(farmRequest1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(farmRequest2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(farmRequest3)))
                .andExpect(status().isCreated());

        // when, then
        mockMvc.perform(get("/api/farms")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].farmName").value("농장1"))
                .andExpect(jsonPath("$[1].farmName").value("농장2"))
                .andExpect(jsonPath("$[2].farmName").value("농장3"));

    }

    @Test
    @DisplayName("내 농장 목록 조회 - 빈 목록")
    void getMyFarms_Success_EmptyList() throws Exception {

        // 농장 없음

        // when, then
        mockMvc.perform(get("/api/farms")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }

    @Test
    @DisplayName("내 농장 목록 조회 실패 - 인증 토큰 없음")
    void getMyFarms_Fail_NoToken() throws Exception {

        // when, then
        mockMvc.perform(get("/api/farms"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("특정 농장 조회 성공")
    void getFarm_Success() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("특정농장")
                .zipCode("12345")
                .street("여기야여기")
                .detail("거기있자너")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        // when, then

        mockMvc.perform(get("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmId").value(farmId))
                .andExpect(jsonPath("$.farmName").value("특정농장"))
                .andExpect(jsonPath("$.zipCode").value("12345"))
                .andExpect(jsonPath("$.street").value("여기야여기"))
                .andExpect(jsonPath("$.detail").value("거기있자너"))
                .andExpect(jsonPath("$.farmSize").value(1000))
                .andExpect(jsonPath("$.memberId").value(testMemberId));

    }

    @Test
    @DisplayName("특정 농장 조회 실패 - 인증 토큰 없음")
    void getFarm_Fail_NoToken() throws Exception {

        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("테스트농장")
                .street("여기여 여기")
                .farmSize(1500L)
                .build();

        String response = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(response);
        long farmId = farmNode.get("farmId").asLong();

        // when, then
        mockMvc.perform(get("/api/farms"))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("특정 농장 조회 실패 - 존재하지 않는 농장 ID")
    void getFarm_Fail_NotFound() throws Exception {

        // given
        Long farmId = 9999L;

        // when, then
        mockMvc.perform(get("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("농장을 찾을 수 없습니다")));
    }

    @Test
    @DisplayName("특정 농장 조회 실패 - 다른 사용자 접근")
    void getFarm_Fail_Forbidden_NotOwner() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("테스트농장")
                .street("서울이셔")
                .farmSize(1000L)
                .build();

        String response = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(response);
        long farmId = farmNode.get("farmId").asLong();

        // 다른 사용자
        RegisterRequest otherUserRequest = RegisterRequest.builder()
                .username("otheruser")
                .password("Pass1234!")
                .passwordConfirm("Pass1234!")
                .name("다른사용자")
                .contact("01011112222")
                .build();

        // 다른 사용자 가입
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherUserRequest)));

        // 다른 사용자 로그인 요청
        LoginRequest otherLoginRequest = LoginRequest.builder()
                .username("otheruser")
                .password("Pass1234!")
                .build();

        // 다른 사용자 로그인
        String otherLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherLoginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode otherNode = objectMapper.readTree(otherLoginResponse);
        String otherAccessToken = otherNode.get("accessToken").asText();

        // when, then
        mockMvc.perform(get("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + otherAccessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("접근 권한이 없습니다")));

    }

    @Test
    @DisplayName("농장 수정 성공 - 전체 필드")
    void updateFarm_Success_AllFields() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("수정할농장")
                .zipCode("12345")
                .street("수정할농장주소")
                .detail("수정할상세주소")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        // 수정 요청
        UpdateFarmRequest updateRequest = UpdateFarmRequest.builder()
                .farmName("수정된농장")
                .zipCode("54321")
                .street("수정된주소")
                .detail("수정된상세주소")
                .farmSize(2000L)
                .build();

        // when, then
        mockMvc.perform(put("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmId").value(farmId))
                .andExpect(jsonPath("$.farmName").value("수정된농장"))
                .andExpect(jsonPath("$.zipCode").value("54321"))
                .andExpect(jsonPath("$.street").value("수정된주소"))
                .andExpect(jsonPath("$.detail").value("수정된상세주소"))
                .andExpect(jsonPath("$.farmSize").value(2000));

    }

    @Test
    @DisplayName("농장 부분 수정 성공 - 농장이름")
    void updateFarm_Success_Name() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("원래농장")
                .zipCode("12345")
                .street("원래주소")
                .detail("원래상세주소")
                .farmSize(1000L)
                .build();

        String response = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(response);
        long farmId = farmNode.get("farmId").asLong();

        UpdateFarmRequest updateRequest = UpdateFarmRequest.builder()
                .farmName("이름만 수정")
                .build();

        // when, then
        mockMvc.perform(put("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmName").value("이름만 수정"))
                .andExpect(jsonPath("$.zipCode").value("12345"))
                .andExpect(jsonPath("$.street").value("원래주소"))
                .andExpect(jsonPath("$.detail").value("원래상세주소"))
                .andExpect(jsonPath("$.farmSize").value("1000"));
    }

    @Test
    @DisplayName("농장 부분 수정 성공 - 주소")
    void updateFarm_Success_AddressOnly() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("원래농장")
                .street("원래주소")
                .farmSize(1000L)
                .build();

        String response = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(response);
        long farmId = farmNode.get("farmId").asLong();

        // 주소만 수정
        UpdateFarmRequest updateRequest = UpdateFarmRequest.builder()
                .zipCode("99999")
                .street("새주소야")
                .detail("새상세주소")
                .build();

        // when, then
        mockMvc.perform(put("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmName").value("원래농장"))
                .andExpect(jsonPath("$.zipCode").value("99999"))
                .andExpect(jsonPath("$.street").value("새주소야"))
                .andExpect(jsonPath("$.detail").value("새상세주소"))
                .andExpect(jsonPath("$.farmSize").value(1000));

    }

    @Test
    @DisplayName("농장 수정 실패 - 토큰 없음")
    void updateFarm_Fail_NoToken() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("테스트농장")
                .street("주소")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        UpdateFarmRequest updateRequest = UpdateFarmRequest.builder()
                .farmName("농장이름수정")
                .build();

        // when, then
        mockMvc.perform(put("/api/farms/{farmId}", farmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("농장 수정 실패 - 존재하지 않는 농장 ID")
    void updateFarm_Fail_NotFound() throws Exception {

        // given
        Long nonExistentFarmId = 9999L;
        UpdateFarmRequest updateRequest = UpdateFarmRequest.builder()
                .farmName("수정농장")
                .build();

        // when, then
        mockMvc.perform(put("/api/farms/{farmId}", nonExistentFarmId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("농장을 찾을 수 없습니다")));
    }

    @Test
    @DisplayName("농장 수정 실패 - 다른 사용자 접근")
    void updateFarm_Fail_Forbidden_NotOwner() throws Exception {

        // given
        CreateFarmRequest createRequest = CreateFarmRequest.builder()
                .farmName("테스트농장")
                .street("주소")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        // 다른 사용자
        RegisterRequest otherUserRequest = RegisterRequest.builder()
                .username("otherUser")
                .password("Pass1234!")
                .passwordConfirm("Pass1234!")
                .name("다른사용자")
                .contact("01022221111")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherUserRequest)));

        LoginRequest otherUserLoginRequest = LoginRequest.builder()
                .username("otherUser")
                .password("Pass1234!")
                .build();

        String otherLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherUserLoginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode otherUserNode = objectMapper.readTree(otherLoginResponse);
        String otherAccessToken = otherUserNode.get("accessToken").asText();

        UpdateFarmRequest updateRequest = UpdateFarmRequest.builder()
                .farmName("수정시도222")
                .build();

        // when, then
        mockMvc.perform(put("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + otherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("접근 권한이 없습니다")));
    }

    @Test
    @DisplayName("농장 수정 실패 - 빈 농장이름")
    void updateFarm_Fail_EmptyFarmName() throws Exception {

        // given
        CreateFarmRequest createRequest = CreateFarmRequest.builder()
                .farmName("원래농장")
                .street("주소")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        UpdateFarmRequest updateRequest = UpdateFarmRequest.builder()
                .farmName("")
                .build();

        // when, then
        mockMvc.perform(put("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("수정할 정보가 없습니다")));

    }

    @Test
    @DisplayName("농장 수정 실패 - 농장명 길이 초과")
    void updateFarm_Fail_FarmNameTooLong() throws Exception {

        // given
        CreateFarmRequest createRequest = CreateFarmRequest.builder()
                .farmName("원래농장")
                .street("주소")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        UpdateFarmRequest updateRequest = UpdateFarmRequest.builder()
                .farmName("ㅇ".repeat(200))
                .build();

        // when, then
        mockMvc.perform(put("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.farmName").value(containsString("20자 이하")));

    }

    @Test
    @DisplayName("농장 수정 실패 - 농장 크기 0")
    void updateFarm_Fail_FarmSize_Zero() throws Exception {

        // given
        CreateFarmRequest createRequest = CreateFarmRequest.builder()
                .farmName("원래농장")
                .street("주소")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        UpdateFarmRequest updateRequest = UpdateFarmRequest.builder()
                .farmSize(0L)
                .build();

        // when, then
        mockMvc.perform(put("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.farmSize").value(containsString("1 이상")));

    }

    @Test
    @DisplayName("농장 삭제 성공")
    void deleteFarm_Success() throws Exception {

        // given
        CreateFarmRequest createRequest = CreateFarmRequest.builder()
                .farmName("삭제될농장")
                .street("주소")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        // when, then
        mockMvc.perform(delete("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 삭제 후 조회 - 404
        mockMvc.perform(get("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("농장 삭제 성공 - 목록 조회 시 제외")
    void deleteFarm_Success_NotInList() throws Exception {

        // given
        CreateFarmRequest createRequest1 = CreateFarmRequest.builder()
                .farmName("농장1")
                .street("주소1")
                .farmSize(1000L)
                .build();

        CreateFarmRequest createRequest2 = CreateFarmRequest.builder()
                .farmName("농장2")
                .street("주소2")
                .farmSize(2000L)
                .build();

        mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest1)))
                .andExpect(status().isCreated());

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        // when
        mockMvc.perform(delete("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/api/farms")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].farmName").value("농장1"));

    }

    @Test
    @DisplayName("농장 삭제 실패 - 인증 토큰 x")
    void deleteFarm_Fail_NoToken() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("테스트농장")
                .street("주소")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        // when, then
        mockMvc.perform(delete("/api/farms/{farmId}", farmId))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("농장 삭제 실패 - 없는 농장 ID")
    void deleteFarm_Fail_NotFound() throws Exception {

        // given
        Long nonExistentFarmId = 9999L;

        // when, then
        mockMvc.perform(delete("/api/farms/{farmId}", nonExistentFarmId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("농장을 찾을 수 없습니다")));

    }

    @Test
    @DisplayName("농장 삭제 실패 - 다른 사용자 접근")
    void deleteFarm_Fail_Forbidden_NotOwner() throws Exception {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("테스트농장")
                .street("주소")
                .farmSize(1000L)
                .build();

        String createResponse = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode farmNode = objectMapper.readTree(createResponse);
        long farmId = farmNode.get("farmId").asLong();

        RegisterRequest otherUserRequest = RegisterRequest.builder()
                .username("otherUser")
                .password("Pass1234!")
                .passwordConfirm("Pass1234!")
                .name("다른 사용자")
                .contact("01012342222")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherUserRequest)));

        LoginRequest otherLoginRequest = LoginRequest.builder()
                .username("otherUser")
                .password("Pass1234!")
                .build();

        String otherLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherLoginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode otherNode = objectMapper.readTree(otherLoginResponse);
        String otherAccessToken = otherNode.get("accessToken").asText();

        // when, then
        mockMvc.perform(delete("/api/farms/{farmId}", farmId)
                        .header("Authorization", "Bearer " + otherAccessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("접근 권한이 없습니다")));
    }
}