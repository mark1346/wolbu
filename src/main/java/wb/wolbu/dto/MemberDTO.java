package wb.wolbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import wb.wolbu.entity.Member;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    @Schema(description = "Member ID", example = "1")
    private Long id;

    @Schema(description = "Member name", example = "John Doe")
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @Schema(description = "Member email", example = "john.doe@example.com")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    private String email;

    @Schema(description = "Member phone number", example = "010-1234-5678")
    @NotBlank(message = "휴대폰 번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNumber;

    @Schema(description = "Member password", example = "password123")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

    @Schema(description = "Member type", example = "STUDENT")
    @NotBlank(message = "회원 유형은 필수 입력 항목입니다.")
    private String memberType;

    // Member -> MemberDTO
    public static MemberDTO from(Member member) {
        return MemberDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .memberType(member.getMemberType().name())
                .build();
    }
}
