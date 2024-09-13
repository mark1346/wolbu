package wb.wolbu.dto;

import lombok.*;
import wb.wolbu.entity.Course;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private Long id;
    private String name;
    private Integer maxStudents;
    private Integer price;
    private Integer currentEnrollmentCount;
    private String instructorName;
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
