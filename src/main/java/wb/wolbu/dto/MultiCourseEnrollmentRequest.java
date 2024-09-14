package wb.wolbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Student ID", example = "1")
    @NotNull(message = "학생 ID는 필수입니다.")
    private Long studentId;

    @Schema(description = "List of course IDs", example = "[1, 2, 3]")
    @NotEmpty(message = "강의 ID는 필수입니다.")
    private List<Long> courseIds;
}
