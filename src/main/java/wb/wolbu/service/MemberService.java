package wb.wolbu.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
public class MemberService  implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member registerMember(String name, String email, String phoneNumber, String password, MemberType memberType) {
        if (!PasswordValidator.isValid(password)) {
            throw new BusinessLogicException("비밀번호는 6자 이상 10자 이하이며, 영문 소문자, 대문자, 숫자 중 최소 두 가지 이상을 조합해야 합니다.");
        }
        if (memberRepository.existsByEmail(phoneNumber)) {
            throw new BusinessLogicException("이미 등록된 휴대폰 번호입니다.");
        }
        String encodedPassword = passwordEncoder.encode(password);
        Member member = new Member(name, email, phoneNumber, encodedPassword, memberType);
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
    }


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Member member = findMemberByEmail(email);
            return org.springframework.security.core.userdetails.User.builder()
                    .username(member.getPhoneNumber())
                    .password(member.getPassword())
                    .roles(member.getMemberType().name())
                    .build();
        } catch (EntityNotFoundException e) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
        }
    }

}
