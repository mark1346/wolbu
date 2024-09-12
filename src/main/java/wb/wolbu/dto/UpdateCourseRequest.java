package wb.wolbu.dto;


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
public class UpdateCourseRequest {
    @NotBlank(message = "강의명은 필수입니다.")
    @Size(max = 100, message = "강의명은 100자를 초과할 수 없습니다.")
    private String name;

    @NotNull(message = "최대 수강 인원은 필수입니다.")
    @Min(value = 1, message = "최대 수강 인원은 1명 이상이어야 합니다.")
    private Integer maxStudents;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;
}