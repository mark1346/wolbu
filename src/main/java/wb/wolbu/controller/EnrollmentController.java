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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollment Management", description = "APIs for managing course enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @Operation(summary = "Enroll in a course", description = "Enrolls a student in a course")
    @ApiResponse(responseCode = "201", description = "Successfully enrolled",
            content = @Content(schema = @Schema(implementation = EnrollmentDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Enrollment conflict")
    public ResponseEntity<EnrollmentDTO> enrollCourse(@RequestBody @Valid EnrollmentRequest request){
        Enrollment enrollment = enrollmentService.enrollCourse(request.getStudentId(), request.getCourseId());
        return new ResponseEntity<>(EnrollmentDTO.from(enrollment), HttpStatus.CREATED);
    }

    @PostMapping("/multiple")
    @Operation(summary = "Enroll in multiple courses", description = "Enrolls a student in multiple courses")
    @ApiResponse(responseCode = "201", description = "Successfully enrolled in courses",
            content = @Content(schema = @Schema(implementation = List.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<List<EnrollmentResult>> enrollMultipleCourses(@RequestBody @Valid MultiCourseEnrollmentRequest request){
        List<EnrollmentResult> results = enrollmentService.enrollMultipleCourses(request.getStudentId(), request.getCourseIds());
        return new ResponseEntity<>(results, HttpStatus.CREATED);
    }

    @DeleteMapping
    @Operation(summary = "Cancel enrollment", description = "Cancels a student's enrollment in a course")
    @ApiResponse(responseCode = "204", description = "Successfully canceled enrollment")
    @ApiResponse(responseCode = "404", description = "Enrollment not found")
    public ResponseEntity<Void> cancelEnrollment(@RequestBody @Valid EnrollmentRequest request){
        enrollmentService.cancelEnrollment(request.getStudentId(), request.getCourseId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get student enrollments", description = "Retrieves all enrollments for a specific student")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved enrollments",
            content = @Content(schema = @Schema(implementation = List.class)))
    @ApiResponse(responseCode = "404", description = "Student not found")
    public ResponseEntity<List<EnrollmentDTO>> getStudentEnrollments(@PathVariable Long studentId){
        List<EnrollmentDTO> enrollmentDTOs = enrollmentService.getStudentEnrollments(studentId)
                .stream()
                .map(EnrollmentDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollmentDTOs);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get course enrollments", description = "Retrieves all enrollments for a specific course")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved enrollments",
            content = @Content(schema = @Schema(implementation = List.class)))
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<List<EnrollmentDTO>> getCourseEnrollments(@PathVariable Long courseId){
        List<EnrollmentDTO> enrollmentDTOs = enrollmentService.getCourseEnrollments(courseId)
                .stream()
                .map(EnrollmentDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollmentDTOs);
    }
}
