package wb.wolbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {
    @Schema(description = "Instructor ID", example = "1")
    @NotNull(message = "강사 ID는 필수입니다.")
    private Long instructorId;

    @Schema(description = "Course name", example = "Advanced Java Programming")
    @NotBlank(message = "강의명은 필수입니다.")
    @Size(max = 100, message = "강의명은 100자를 초과할 수 없습니다.")
    private String name;

    @Schema(description = "Maximum number of students", example = "25")
    @NotNull(message = "최대 수강 인원은 필수입니다.")
    @Min(value = 1, message = "최대 수강 인원은 1명 이상이어야 합니다.")
    private Integer maxStudents;

    @Schema(description = "Course price", example = "150000")
    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;
}
