package wb.wolbu.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import wb.wolbu.dto.CourseDTO;
import wb.wolbu.dto.CreateCourseRequest;
import wb.wolbu.dto.UpdateCourseRequest;
import wb.wolbu.entity.Course;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.repository.CourseRepository;
import wb.wolbu.repository.MemberRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseIntegraionTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private MemberRepository memberRepository;

    private static Long courseId;
    private static Long instructorId;

    @BeforeAll
    public void setUp() {
        // 강사 생성
        Member instructor = new Member("Instructor", "instructor@example.com", "010-1111-2222", "Password1!", MemberType.INSTRUCTOR);
        instructor = memberRepository.save(instructor);
        instructorId = instructor.getId();

        // 강좌 생성
        Course course = new Course("Initial Test Course", 30, 100000, instructor);
        course = courseRepository.save(course);
        courseId = course.getId();
    }

    @Test
    @Order(1)
    @DisplayName("강의 등록 테스트")
    @WithMockUser(username = "mark", roles = "INSTRUCTOR")
    public void testCreateCourse() throws Exception {
        CreateCourseRequest courseRequest = new CreateCourseRequest(
                instructorId,
                "Spring Boot Master Class",
                30,
                100000
        );

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Spring Boot Master Class"))
                .andExpect(jsonPath("$.maxStudents").value(30))
                .andExpect(jsonPath("$.price").value(100000))
                .andDo(result -> {
                    CourseDTO createdCourse = objectMapper.readValue(result.getResponse().getContentAsString(), CourseDTO.class);
                    courseId = createdCourse.getId();
                });
    }

    @Test
    @Order(2)
    @DisplayName("강의 목록 조회 테스트")
    public void testGetCourses() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "recent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Initial Test Course"));
    }

    @Test
    @Order(3)
    @DisplayName("강의 상세 조회 테스트")
    public void testGetCourse() throws Exception {
        mockMvc.perform(get("/api/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Initial Test Course"))
                .andExpect(jsonPath("$.maxStudents").value(30))
                .andExpect(jsonPath("$.price").value(100000));
    }

    @Test
    @Order(4)
    @DisplayName("강사별 강의 목록 조회 테스트")
    public void testGetInstructorCourses() throws Exception {
        mockMvc.perform(get("/api/courses/instructor/" + instructorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Initial Test Course"));
    }

    @Test
    @Order(5)
    @DisplayName("강의 수정 테스트")
    @WithMockUser(username = "mark", roles = "INSTRUCTOR")
    public void testUpdateCourse() throws Exception {
        UpdateCourseRequest updateRequest = new UpdateCourseRequest(
                "Advanced Spring Boot",
                35,
                120000
        );

        mockMvc.perform(put("/api/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Advanced Spring Boot"))
                .andExpect(jsonPath("$.maxStudents").value(35))
                .andExpect(jsonPath("$.price").value(120000));
    }

    @Test
    @Order(6)
    @DisplayName("강의 삭제 테스트")
    @WithMockUser(username = "instructor@example.com", roles = "INSTRUCTOR")
    public void testDeleteCourse() throws Exception {
        mockMvc.perform(delete("/api/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertFalse(courseRepository.existsById(courseId));
    }

}
