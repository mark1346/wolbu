package wb.wolbu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wb.wolbu.entity.Member;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private Long id;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    private String email;

    @NotBlank(message = "휴대폰 번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNumber;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

    @NotBlank(message = "회원 유형은 필수 입력 항목입니다.")
    private String memberType;

    // Member -> MemberDTO
    public static MemberDTO from(Member member) {
        return new MemberDTO(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPhoneNumber(),
                "", // 보안상 비밀번호는 반환하지 않습니다.
                member.getMemberType().name()
        );
    }
}
