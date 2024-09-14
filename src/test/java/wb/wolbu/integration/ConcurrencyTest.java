package wb.wolbu.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import wb.wolbu.dto.EnrollmentRequest;
import wb.wolbu.entity.Course;
import wb.wolbu.entity.Enrollment;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.repository.CourseRepository;
import wb.wolbu.repository.EnrollmentRepository;
import wb.wolbu.repository.MemberRepository;
import wb.wolbu.service.EnrollmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConcurrencyTest {
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private ObjectMapper objectMapper;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    EnrollmentService enrollmentService;

    private static Long courseId;
    private static List<Long> studentIds = new ArrayList<>();

    @BeforeAll
    public void setUp() {
        // 강사 생성
        Member instructor = new Member("Instructor", "instructor@example.com", "010-1111-2222", "Password1!", MemberType.INSTRUCTOR);
        instructor = memberRepository.save(instructor);

        // 강좌 생성 (최대 수강 인원: 10명)
        Course course = new Course("Concurrent Test Course", 10, 100000, instructor);
        course = courseRepository.save(course);
        courseId = course.getId();

        // 15명의 학생 생성
        for (int i = 0; i < 15; i++) {
            Member student = new Member("Student" + i, "student" + i + "@example.com", "010-0000-" + String.format("%04d", i), "Password1!", MemberType.STUDENT);
            student = memberRepository.save(student);
            studentIds.add(student.getId());
        }
    }

    @Test
    @DisplayName("수강 신청 시 currentEnrollmentCount 증가하나 확인")
    void enrollCourse_ShouldIncreaseCurrentEnrollmentCount() {
        // Given
        Member instructor2 = new Member("Instructor", "instructor@test.com", "010-1234-5678", "password", MemberType.INSTRUCTOR);
        memberRepository.save(instructor2);

        Course course = new Course("Test Course", 10, 100000, instructor2);
        courseRepository.save(course);
        Member student2 = new Member("Student", "student@test.com", "010-8765-4321", "password", MemberType.STUDENT);
        memberRepository.save(student2);

        Integer initialCount = course.getCurrentEnrollmentCount();

        // When
        enrollmentService.enrollCourse(student2.getId(), course.getId());

        // Then
        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updatedCourse.getCurrentEnrollmentCount()).isEqualTo(initialCount + 1);
    }

    @Test
    @Order(1)
    @DisplayName("동시 수강 신청 테스트")
    @WithMockUser(username = "asd", roles = "STUDENT")
    public void testConcurrentEnrollment() throws Exception {
        int threadCount = 15;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            futures.add(executorService.submit(() -> {
                try {
                    startLatch.await();
                    enrollmentService.enrollCourse(studentIds.get(index), courseId);
                    return true;
                } catch (Exception e) {
                    System.out.println("수강 신청 실패 - 학생 ID: " + studentIds.get(index) + ", 오류: " + e.getMessage());
                    return false;
                } finally {
                    latch.countDown();
                }
            }));
        }

        startLatch.countDown();
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        Course finalCourse = courseRepository.findById(courseId).orElseThrow();
        List<Enrollment> allEnrollments = enrollmentRepository.findByCourse(finalCourse);

        System.out.println("최종 수강 인원: " + finalCourse.getCurrentEnrollmentCount());
        System.out.println("실제 등록된 수강 정보 수: " + allEnrollments.size());
        System.out.println("성공한 수강 신청 수: " + successCount);

        assertEquals(10, finalCourse.getCurrentEnrollmentCount());
        assertEquals(10, allEnrollments.size());
        assertEquals(10, successCount);
    }

}
