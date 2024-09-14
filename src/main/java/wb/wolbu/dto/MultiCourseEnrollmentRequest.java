package wb.wolbu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultiCourseEnrollmentRequest {
    @NotNull(message = "학생 ID는 필수입니다.")
    private Long studentId;

    @NotEmpty(message = "강의 ID는 필수입니다.")
    private List<Long> courseIds;
}
