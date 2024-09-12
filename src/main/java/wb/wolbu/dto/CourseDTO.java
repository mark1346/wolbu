package wb.wolbu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wb.wolbu.entity.Course;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String name;
    private Integer maxStudents;
    private Integer price;
    private Integer currentEnrollmentCount;
    private String instructorName;
    private LocalDateTime createdAt;

    public static CourseDTO from(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getName(),
                course.getMaxStudents(),
                course.getPrice(),
                course.getCurrentEnrollmentCount(),
                course.getInstructor().getName(),
                course.getCreatedAt()
        );
    }
}
