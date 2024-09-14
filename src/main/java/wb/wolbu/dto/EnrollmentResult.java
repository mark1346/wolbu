package wb.wolbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EnrollmentResult {
    @Schema(description = "Course ID", example = "1")
    private Long courseId;

    @Schema(description = "Enrollment success status", example = "true")
    private boolean success;

    @Schema(description = "Error message if enrollment failed", example = "Course is full")
    private String errorMessage;

}
