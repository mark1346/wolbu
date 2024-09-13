package wb.wolbu.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.exception.custom.BusinessLogicException;
import wb.wolbu.repository.MemberRepository;
import wb.wolbu.util.PasswordValidator;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member registerMember(String name, String email, String phoneNumber, String password, MemberType memberType) {
        if (!PasswordValidator.isValid(password)) {
            throw new BusinessLogicException("비밀번호는 6자 이상 10자 이하이며, 영문 소문자, 대문자, 숫자 중 최소 두 가지 이상을 조합해야 합니다.");
        }
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessLogicException("이미 등록된 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(password);
        Member member = new Member(name, email, phoneNumber, encodedPassword, memberType);
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다." + email));
    }


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Member member = findMemberByEmail(email);
            return org.springframework.security.core.userdetails.User.builder()
                    .username(member.getEmail())
                    .password(member.getPassword())
                    .roles(member.getMemberType().name())
                    .build();
        } catch (EntityNotFoundException e) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
        }
    }

    // ************************ 추가 기능 ************************

    @Transactional(readOnly = true)
    public Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Member> findAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    // Update 기능
    public Member updateMember(Long id, String name, String phoneNumber, MemberType memberType) {
        Member member = findMemberById(id);
        member.updateInfo(name, phoneNumber, memberType);
        return memberRepository.save(member);
    }

    public void changePassword(Long id, String currentPassword, String newPassword) {
        Member member = findMemberById(id);
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new BusinessLogicException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (!PasswordValidator.isValid(newPassword)) {
            throw new BusinessLogicException("비밀번호는 6자 이상 10자 이하이며, 영문 소문자, 대문자, 숫자 중 최소 두 가지 이상을 조합해야 합니다.");
        }
        member.changePassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    // Delete 기능
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다. ID: " + id);
        }
        memberRepository.deleteById(id);
    }

}
