package wb.wolbu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDTO {

    @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    private String newPassword;
}
