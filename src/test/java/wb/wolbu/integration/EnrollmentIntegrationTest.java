package wb.wolbu.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import wb.wolbu.dto.EnrollmentDTO;
import wb.wolbu.dto.EnrollmentRequest;
import wb.wolbu.entity.Course;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.repository.CourseRepository;
import wb.wolbu.repository.EnrollmentRepository;
import wb.wolbu.repository.MemberRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnrollmentIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CourseRepository courseRepository;

    private static Long studentId;
    private static Long courseId;
    private static Long enrollmentId;

    @BeforeAll
    public void setUp() {
        // Create a student
        Member student = new Member("Student", "student@example.com", "010-1234-5678", "Password1!", MemberType.STUDENT);
        student = memberRepository.save(student);
        studentId = student.getId();

        // Create an instructor
        Member instructor = new Member("Instructor", "instructor@example.com", "010-9876-5432", "Password1!", MemberType.INSTRUCTOR);
        instructor = memberRepository.save(instructor);

        // Create a course
        Course course = new Course("Test Course", 30, 100000, instructor);
        course = courseRepository.save(course);
        courseId = course.getId();
    }

    @Test
    @Order(1)
    @DisplayName("수강 신청 테스트")
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    public void testEnrollCourse() throws Exception {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest(studentId, courseId);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andDo(result -> {
                    EnrollmentDTO createdEnrollment = objectMapper.readValue(result.getResponse().getContentAsString(), EnrollmentDTO.class);
                    enrollmentId = createdEnrollment.getId();
                });
    }

    @Test
    @Order(2)
    @DisplayName("학생별 수강 신청 목록 조회 테스트")
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    public void testGetStudentEnrollments() throws Exception {
        mockMvc.perform(get("/api/enrollments/student/" + studentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(studentId))
                .andExpect(jsonPath("$[0].courseId").value(courseId));
    }

    @Test
    @Order(3)
    @DisplayName("강의별 수강 신청 목록 조회 테스트")
    @WithMockUser(username = "instructor@example.com", roles = "INSTRUCTOR")
    public void testGetCourseEnrollments() throws Exception {
        mockMvc.perform(get("/api/enrollments/course/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(studentId))
                .andExpect(jsonPath("$[0].courseId").value(courseId));
    }

    @Test
    @Order(4)
    @DisplayName("수강 취소 테스트")
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    public void testCancelEnrollment() throws Exception {
        EnrollmentRequest cancelRequest = new EnrollmentRequest(studentId, courseId);

        mockMvc.perform(delete("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isNoContent());

        assertFalse(enrollmentRepository.existsById(enrollmentId));
    }

    @Test
    @Order(5)
    @DisplayName("수강 인원 초과 시 신청 실패 테스트")
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    public void testEnrollmentFailureWhenCourseFull() throws Exception {
        // Update course to be full
        Course course = courseRepository.findById(courseId).orElseThrow();
        course.update(course.getName(), 0, course.getPrice());
        courseRepository.save(course);

        EnrollmentRequest enrollmentRequest = new EnrollmentRequest(studentId, courseId);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("수강 신청 인원이 꽉 찼습니다."));
    }
}
