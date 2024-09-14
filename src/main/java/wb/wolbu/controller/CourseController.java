package wb.wolbu.controller;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wb.wolbu.dto.CourseDTO;
import wb.wolbu.dto.CreateCourseRequest;
import wb.wolbu.dto.UpdateCourseRequest;
import wb.wolbu.entity.Course;
import wb.wolbu.service.CourseService;
import wb.wolbu.service.GptService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course Management", description = "APIs for managing courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final GptService gptService;

    @PostMapping
    @Operation(summary = "Create a new course", description = "Creates a new course")
    @ApiResponse(responseCode = "201", description = "Successfully created course",
            content = @Content(schema = @Schema(implementation = CourseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody @Valid CreateCourseRequest request){
        Course course = courseService.createCourse(
                request.getInstructorId(),
                request.getName(),
                request.getMaxStudents(),
                request.getPrice()
        );
        return new ResponseEntity<>(CourseDTO.from(course), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all courses", description = "Retrieves a list of all courses")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved courses",
            content = @Content(schema = @Schema(implementation = Page.class)))
    public ResponseEntity<Page<CourseDTO>> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recent") String sortBy){
        Page<CourseDTO> courseDTOs = courseService.getCourses(page, size, sortBy)
                .map(CourseDTO::from);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course", description = "Retrieves a course by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved course",
            content = @Content(schema = @Schema(implementation = CourseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable Long id){
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(CourseDTO.from(course));
    }

    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Get instructor courses", description = "Retrieves all courses for a specific instructor")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved courses",
            content = @Content(schema = @Schema(implementation = List.class)))
    @ApiResponse(responseCode = "404", description = "Instructor not found")
    public ResponseEntity<List<CourseDTO>> getInstructorCourses(@PathVariable Long instructorId){
        List<CourseDTO> courseDTOs = courseService.getInstructorCourses(instructorId)
                .stream()
                .map(CourseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(courseDTOs);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course", description = "Updates an existing course")
    @ApiResponse(responseCode = "200", description = "Successfully updated course",
            content = @Content(schema = @Schema(implementation = CourseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody @Valid UpdateCourseRequest request){
        Course updateCourse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(CourseDTO.from(updateCourse));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course", description = "Deletes an existing course")
    @ApiResponse(responseCode = "204", description = "Successfully deleted course")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id){
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("recommend")
    @Operation(summary = "Recommend courses", description = "Recommends courses based on the user's goal")
    @ApiResponse(responseCode = "200", description = "Successfully recommended courses",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<String> recommendCourse(@RequestBody Map<String, String> request){
        String userGoal = request.get("userGoal");
        if (userGoal == null || userGoal.isEmpty()) {
            return ResponseEntity.badRequest().body("사용자 목표를 입력해주세요.");
        }

        String recommendation = gptService.getCourseRecommendation(userGoal);
        return ResponseEntity.ok(recommendation);
    }
}
