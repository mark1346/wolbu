package wb.wolbu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EnrollmentResult {
    private Long courseId;
    private boolean success;
    private String errorMessage;
}
