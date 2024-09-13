package wb.wolbu.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.exception.custom.BusinessLogicException;
import wb.wolbu.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private MemberType memberType;

    @BeforeEach
    void setUp() {
        name = "Mark Han";
        email = "mark@example.com";
        phoneNumber = "01012345678";
        password = "Password1!";
        memberType = MemberType.STUDENT;
    }

    @Test
    @DisplayName("회원 가입 - 정상적인 입력이 주어지면 회원을 등록한다.")
    void registerMember_WithValidInput_ShouldRegisterMember() {
        // given
        given(passwordEncoder.encode(password)).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Member registeredMember = memberService.registerMember(name, email, phoneNumber, password, memberType);

        // then
        assertThat(registeredMember).isNotNull();
        assertThat(registeredMember.getName()).isEqualTo(name);
        assertThat(registeredMember.getEmail()).isEqualTo(email);
        assertThat(registeredMember.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(registeredMember.getPassword()).isEqualTo("encodedPassword");
        assertThat(registeredMember.getMemberType()).isEqualTo(memberType);

        verify(passwordEncoder).encode(password);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 가입 - 비밀번호가 유효하지 않으면 BusinessLogicException을 던진다.")
    void registerMember_WithInvalidPassword_ShouldThrowBusinessLogicException() {
        // given
        password = "weak";

        // when
        // then
        assertThatThrownBy(() -> memberService.registerMember(name, email, phoneNumber, password, memberType))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("비밀번호는 6자 이상 10자 이하이며, 영문 소문자, 대문자, 숫자 중 최소 두 가지 이상을 조합해야 합니다.");
    }

    @Test
    @DisplayName("회원 가입 - 중복된 이메일이 주어지면 BusinessLogicException을 던진다.")
    void registerMember_WithDuplicateEmail_ShouldThrowBusinessLogicException() {
        // given
        given(memberRepository.existsByEmail(email)).willReturn(true);

        // when
        // then
        assertThatThrownBy(() -> memberService.registerMember(name, email, phoneNumber, password, memberType))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("이미 등록된 이메일입니다.");
    }

    @Test
    @DisplayName("이메일로 회원 찾기")
    void findMemberByEmail_WithExistingEmail_ShouldReturnMember() {
        // given
        Member member = new Member(name, email, phoneNumber, password, memberType);
        given(memberRepository.findByEmail(email)).willReturn(java.util.Optional.of(member));

        // when
        Member foundMember = memberService.findMemberByEmail(email);

        // then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember).isEqualTo(member);
    }

    @Test
    @DisplayName("이메일로 회원 찾기 - 존재하지 않는 이메일이 주어지면 EntityNotFoundException을 던진다.")
    void findMemberByEmail_WithNonExistingEmail_ShouldThrowEntityNotFoundException() {
        // given
        given(memberRepository.findByEmail(email)).willReturn(java.util.Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> memberService.findMemberByEmail(email))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다." + email);
    }

    @Test
    @DisplayName("스프링 시큐리티 UserDetailsService구현 - 사용자가 존재하면 UserDetails를 반환한다.")
    void loadUserByUsername_WithExistingEmail_ShouldReturnUserDetails() {
        // given
        Member member = new Member(name, email, phoneNumber, password, memberType);
        given(memberRepository.findByEmail(email)).willReturn(java.util.Optional.of(member));

        // when
        UserDetails userDetails = memberService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo(password);
        assertThat(userDetails.getAuthorities()).isNotEmpty();
    }

    @Test
    @DisplayName("스프링 시큐리티 UserDetailsService구현 - 사용자가 존재하지 않으면 UsernameNotFoundException을 던진다.")
    void loadUserByUsername_WithNonExistingEmail_ShouldThrowUsernameNotFoundException() {
        // given
        given(memberRepository.findByEmail(email)).willReturn(java.util.Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> memberService.loadUserByUsername(email))
                .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다: " + email);
    }

    @Test
    @DisplayName("회원 수정 - 존재하는 회원 ID가 주어지면 회원 정보를 수정한다.")
    void updateMember_WithValidInput_ShouldUpdateMember() {
        // given
        Long id = 1L;
        String updatedName = "Updated Name";
        String updatedPhoneNumber = "01098765432";
        MemberType updatedMemberType = MemberType.INSTRUCTOR;
        Member member = new Member(name, email, phoneNumber, password, memberType);
        given(memberRepository.findById(id)).willReturn(java.util.Optional.of(member));
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Member updatedMember = memberService.updateMember(id, updatedName, updatedPhoneNumber, updatedMemberType);

        // then
        assertThat(updatedMember).isNotNull();
        assertThat(updatedMember.getName()).isEqualTo(updatedName);
        assertThat(updatedMember.getPhoneNumber()).isEqualTo(updatedPhoneNumber);
        assertThat(updatedMember.getMemberType()).isEqualTo(updatedMemberType);

        verify(memberRepository).findById(id);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("비밀번호 수정")
    void changePassword_WithValidInput_ShouldChangePassword() {
        // given
        Long id = 1L;
        String currentPassword = "Password1!";
        String newPassword = "Password2!";
        Member member = new Member(name, email, phoneNumber, password, memberType);
        given(memberRepository.findById(id)).willReturn(java.util.Optional.of(member));
        given(passwordEncoder.matches(currentPassword, member.getPassword())).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn("encodedNewPassword");
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        memberService.changePassword(id, currentPassword, newPassword);

        // then
        verify(memberRepository).findById(id);
        assertThat(member.getPassword()).isEqualTo("encodedNewPassword");
        verify(passwordEncoder).matches(currentPassword, currentPassword);
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    @DisplayName("비밀번호 수정 - 현재 비밀번호가 일치하지 않으면 BusinessLogicException을 던진다.")
    void changePassword_WithIncorrectCurrentPassword_ShouldThrowBusinessLogicException() {
        // given
        Long id = 1L;
        String currentPassword = "Invalid1";
        String newPassword = "Password2!";
        Member member = new Member(name, email, phoneNumber, password, memberType);
        given(memberRepository.findById(id)).willReturn(java.util.Optional.of(member));
        given(passwordEncoder.matches(currentPassword, member.getPassword())).willReturn(false);

        // when
        // then
        assertThatThrownBy(() -> memberService.changePassword(id, currentPassword, newPassword))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("현재 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("회원 삭제")
    void deleteMember_WithExistingId_ShouldDeleteMember() {
        // given
        Long id = 1L;
        Member member = new Member(name, email, phoneNumber, password, memberType);
        given(memberRepository.existsById(id)).willReturn(true);

        // when
        memberService.deleteMember(id);

        // then
        verify(memberRepository).deleteById(id);
    }

    @Test
    @DisplayName("회원 삭제 - 존재하지 않는 ID가 주어지면 EntityNotFoundException을 던진다.")
    void deleteMember_WithNonExistingId_ShouldThrowEntityNotFoundException() {
        // given
        Long id = 999L;
        given(memberRepository.existsById(id)).willReturn(false);

        // when
        // then
        assertThatThrownBy(() -> memberService.deleteMember(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다. ID: " + id);
    }
}