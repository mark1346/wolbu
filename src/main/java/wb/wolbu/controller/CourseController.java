package wb.wolbu.controller;

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

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final GptService gptService;

    @PostMapping
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
    public ResponseEntity<Page<CourseDTO>> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recent") String sortBy){
        Page<CourseDTO> courseDTOs = courseService.getCourses(page, size, sortBy)
                .map(CourseDTO::from);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable Long id){
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(CourseDTO.from(course));
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseDTO>> getInstructorCourses(@PathVariable Long instructorId){
        List<CourseDTO> courseDTOs = courseService.getInstructorCourses(instructorId)
                .stream()
                .map(CourseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(courseDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody @Valid UpdateCourseRequest request){
        Course updateCourse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(CourseDTO.from(updateCourse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id){
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("recommend")
    public ResponseEntity<String> recommendCourse(@RequestBody Map<String, String> request){
        String userGoal = request.get("userGoal");
        if (userGoal == null || userGoal.isEmpty()) {
            return ResponseEntity.badRequest().body("사용자 목표를 입력해주세요.");
        }

        String recommendation = gptService.getCourseRecommendation(userGoal);
        return ResponseEntity.ok(recommendation);
    }
}
