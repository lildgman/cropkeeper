package com.cropkeeper.domain.member.service;

import com.cropkeeper.domain.member.dto.request.UpdateMemberInfoRequest;
import com.cropkeeper.domain.member.dto.request.UpdatePasswordRequest;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.entity.MemberRole;
import com.cropkeeper.domain.member.exception.AlreadyDeletedException;
import com.cropkeeper.domain.member.exception.InvalidMemberRequestException;
import com.cropkeeper.domain.member.exception.MemberNotFoundException;
import com.cropkeeper.domain.member.exception.PasswordMismatchException;
import com.cropkeeper.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 조회 성공")
    void findById_success() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member found = memberService.findById(memberId);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getMemberId()).isEqualTo(memberId);
        assertThat(found.getUsername()).isEqualTo("testuser01");
        assertThat(found.getName()).isEqualTo("test");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 조회 실패 - 존재하지 않는 회원")
    void findById_NotFound() {

        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> memberService.findById(memberId))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 이름, 연락처 모두 수정")
    void updateMemberInfo_Success_BothFields() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test01")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("test02")
                .contact("01098774423")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member updatedMember = memberService.updateMemberInfo(memberId, request);

        // then
        assertThat(updatedMember.getName()).isEqualTo("test02");
        assertThat(updatedMember.getContact()).isEqualTo("01098774423");

        verify(memberRepository, times(1)).findById(memberId);

    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 이름만 수정")
    void updateMemberInfo_Success_Name() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test01")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("test02")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when

        Member updatedMember = memberService.updateMemberInfo(memberId, request);

        // then
        assertThat(updatedMember.getName()).isEqualTo("test02");
        assertThat(updatedMember.getContact()).isEqualTo("01012345678");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 연락처만 수정")
    void updateMemberInfo_Success_Contact() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test01")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .contact("01098765432")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member updatedMember = memberService.updateMemberInfo(memberId, request);

        // then
        assertThat(updatedMember.getName()).isEqualTo("test01");
        assertThat(updatedMember.getContact()).isEqualTo("01098765432");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 존재하지 않는 회원")
    void updateMemberInfo_Fail_MemberNotFound() {

        // given
        Long memberId = 999L;
        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("test02")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> memberService.updateMemberInfo(memberId, request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 빈 문자열")
    void updateMemberInfo_Fail_EmptyString() {

        // given
        Long memberId = 1L;
        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("")
                .contact("")
                .build();

        // when, then
        assertThatThrownBy(() -> memberService.updateMemberInfo(memberId, request))
                .isInstanceOf(InvalidMemberRequestException.class)
                .hasMessage("수정할 정보가 없습니다. 수정할 이름 또는 연락처를 입력해주세요.");

        verify(memberRepository, never()).findById(any());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {

        // given
        Long memberId = 1L;
        String currentPassword = "oldpass1234";
        String encodedCurrentPassword = "encodedOldPassword";
        String newPassword = "newPass1234";

        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password(encodedCurrentPassword)
                .name("test")
                .role(MemberRole.USER)
                .build();

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .newPasswordConfirm(newPassword)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, encodedCurrentPassword)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // when
        memberService.changePassword(memberId, request);

        // then
        verify(memberRepository, times(1)).findById(memberId);
        verify(passwordEncoder, times(1)).matches(currentPassword, encodedCurrentPassword);
        verify(passwordEncoder, times(1)).encode(newPassword);

    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_Fail_WrongCurrentPassword() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test")
                .role(MemberRole.USER)
                .build();

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPassword1234")
                .newPasswordConfirm("newPassword1234")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> memberService.changePassword(memberId, request))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 불일치")
    void changePassword_Fail_NewPasswordMismatch() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test")
                .role(MemberRole.USER)
                .build();

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("oldPass1234")
                .newPassword("newPass1234")
                .newPasswordConfirm("diffNewPass1234")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("oldPass1234", "encodedPassword")).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> memberService.changePassword(memberId, request))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage("새 비밀번호가 일치하지 않습니다.");

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 현재 비밀번호와 일치")
    void changePassword_Fail_SameCurrentPassword() {

        // given
        Long memberId = 1L;
        String currentPassword = "pass1234";
        String encodedPassword = "encodedPassword";

        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password(encodedPassword)
                .name("홍길동")
                .role(MemberRole.USER)
                .build();

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword(currentPassword)
                .newPassword(currentPassword)
                .newPasswordConfirm(currentPassword)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(currentPassword, encodedPassword)).thenReturn(true);

        assertThatThrownBy(() -> memberService.changePassword(memberId, request))
                .isInstanceOf(InvalidMemberRequestException.class)
                .hasMessage("새 비밀번호는 현재 비밀번호와 달라야합니다.");

        verify(passwordEncoder, never()).encode(anyString());

    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteMember_Success() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test01")
                .role(MemberRole.USER)
                .deleted(false)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        memberService.deleteMember(memberId);

        // then
        assertThat(member.isDeleted()).isTrue();
        assertThat(member.getDeletedAt()).isNotNull();

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 회원")
    void deleteMember_Fail_MemberNotFound() {

        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.deleteMember(memberId))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");

        verify(memberRepository, times(1)).findById(memberId);

    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 이미 탈퇴한 회원")
    void deleteMember_Fail_AlreadyDeleted() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test01")
                .role(MemberRole.USER)
                .deleted(false)
                .build();

        member.delete();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when, then
        assertThatThrownBy(() -> memberService.deleteMember(memberId))
                .isInstanceOf(AlreadyDeletedException.class)
                .hasMessageContaining("이미 탈퇴한 회원입니다");

        verify(memberRepository, times(1)).findById(memberId);

    }
}