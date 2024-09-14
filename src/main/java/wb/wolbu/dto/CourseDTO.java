package wb.wolbu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import wb.wolbu.entity.Course;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    @Schema(description = "Course ID", example = "1")
    private Long id;

    @Schema(description = "Course name", example = "Spring Boot Master Class")
    private String name;

    @Schema(description = "Maximum number of students", example = "30")
    private Integer maxStudents;

    @Schema(description = "Course price", example = "100000")
    private Integer price;

    @Schema(description = "Current number of enrolled students", example = "15")
    private Integer currentEnrollmentCount;

    @Schema(description = "Instructor name", example = "John Doe")
    private String instructorName;

    @Schema(description = "Course creation date", example = "2023-09-15T14:30:00")
    private LocalDateTime createdAt;

    public static CourseDTO from(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .maxStudents(course.getMaxStudents())
                .price(course.getPrice())
                .currentEnrollmentCount(course.getCurrentEnrollmentCount())
                .instructorName(course.getInstructor().getName())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
