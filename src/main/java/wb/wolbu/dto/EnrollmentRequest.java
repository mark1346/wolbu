package wb.wolbu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {
    @NotNull(message = "학생 ID는 필수입니다.")
    private Long studentId;

    @NotNull(message = "강의 ID는 필수입니다.")
    private Long courseId;
}
