package wb.wolbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wb.wolbu.entity.Enrollment;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDTO {
    @Schema(description = "Enrollment ID", example = "1")
    private Long id;

    @Schema(description = "Student ID", example = "1")
    private Long studentId;

    @Schema(description = "Student name", example = "Alice Johnson")
    private String studentName;

    @Schema(description = "Course ID", example = "1")
    private Long courseId;

    @Schema(description = "Course name", example = "Spring Boot Master Class")
    private String courseName;

    @Schema(description = "Enrollment date", example = "2023-09-15T14:30:00")
    private LocalDateTime enrollmentDate;

    public static EnrollmentDTO from(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getName())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getName())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .build();
    }
}
