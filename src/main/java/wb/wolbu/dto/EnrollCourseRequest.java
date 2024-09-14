package wb.wolbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollCourseRequest {
    @Schema(description = "Student ID", example = "1")
    @NotNull(message = "학생 ID는 필수입니다.")
    private Long studentId;
}
