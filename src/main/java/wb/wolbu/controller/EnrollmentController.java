package wb.wolbu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wb.wolbu.dto.EnrollmentDTO;
import wb.wolbu.dto.EnrollmentRequest;
import wb.wolbu.dto.EnrollmentResult;
import wb.wolbu.dto.MultiCourseEnrollmentRequest;
import wb.wolbu.entity.Enrollment;
import wb.wolbu.service.EnrollmentService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<EnrollmentDTO> enrollCourse(@RequestBody @Valid EnrollmentRequest request){
        Enrollment enrollment = enrollmentService.enrollCourse(request.getStudentId(), request.getCourseId());
        return new ResponseEntity<>(EnrollmentDTO.from(enrollment), HttpStatus.CREATED);
    }

    @PostMapping("/multiple")
    public ResponseEntity<List<EnrollmentResult>> enrollMultipleCourses(@RequestBody @Valid MultiCourseEnrollmentRequest request){
        List<EnrollmentResult> results = enrollmentService.enrollMultipleCourses(request.getStudentId(), request.getCourseIds());
        return new ResponseEntity<>(results, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Void> cancelEnrollment(@RequestBody @Valid EnrollmentRequest request){
        enrollmentService.cancelEnrollment(request.getStudentId(), request.getCourseId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentDTO>> getStudentEnrollments(@PathVariable Long studentId){
        List<EnrollmentDTO> enrollmentDTOs = enrollmentService.getStudentEnrollments(studentId)
                .stream()
                .map(EnrollmentDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollmentDTOs);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentDTO>> getCourseEnrollments(@PathVariable Long courseId){
        List<EnrollmentDTO> enrollmentDTOs = enrollmentService.getCourseEnrollments(courseId)
                .stream()
                .map(EnrollmentDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollmentDTOs);
    }
}
