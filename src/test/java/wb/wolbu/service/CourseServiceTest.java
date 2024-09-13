package wb.wolbu.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import wb.wolbu.entity.Course;
import wb.wolbu.entity.Enrollment;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.repository.CourseRepository;
import wb.wolbu.repository.EnrollmentRepository;
import wb.wolbu.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CourseService courseService;

    private Member instructor;
    private Course course;
    private List<Course> courses;

    @BeforeEach
    void setUp() {
        instructor = new Member(
                "Mark Han",
                "markymark331@gmail.com",
                "01012345678",
                "Password1!",
                MemberType.INSTURUCTOR);

        course = new Course(
                "Test Course",
                10,
                100000,
                instructor
        );

        courses = Arrays.asList(
                new Course("Java Basic", 10, 100000, instructor),
                new Course("Spring Framework", 15, 150000, instructor),
                new Course("JPA Mastery", 20, 200000, instructor),
                new Course("Microservices Architecture", 25, 250000, instructor),
                new Course("Docker and Kubernetes", 30, 300000, instructor)
        );
    }

    @Test
    @DisplayName("강좌 생성 - 유효한 입력이 주어지면 강좌를 생성한다")
    void createCourse_WithValidInput_ShouldCreateCourse() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(instructor));
        given(courseRepository.save(any(Course.class))).willReturn(course);

        // when
        Course createdCourse = courseService.createCourse(1L, "Test Course", 10, 100000);

        // then
        assertThat(createdCourse).isNotNull();
        assertThat(createdCourse.getName()).isEqualTo("Test Course");
        assertThat(createdCourse.getMaxStudents()).isEqualTo(10);
        assertThat(createdCourse.getPrice()).isEqualTo(100000);
        assertThat(createdCourse.getInstructor()).isEqualTo(instructor);

        verify(memberRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("강좌 생성 - 존재하지 않는 강사 ID로 강좌를 생성하려 하면 EntityNotFoundException을 던진다")
    void createCourse_WithNonExistingInstructorId_ShouldThrowEntityNotFoundException() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> courseService.createCourse(999L, "Test Course", 10, 100000))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("해당 강사를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("강좌 목록 조회 - 최근 등록순으로 정렬")
    void getCourses_SortByRecent_ShouldReturnSortedCourses() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<Course> coursePage = new PageImpl<>(courses.subList(0, 3), pageRequest, courses.size());

        given(courseRepository.findAllByOrderByCreatedAtDesc(pageRequest)).willReturn(coursePage);

        // when
        Page<Course> result = courseService.getCourses(0, 3, "recent");

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Java Basic");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Spring Framework");
        assertThat(result.getContent().get(2).getName()).isEqualTo("JPA Mastery");
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);

        verify(courseRepository).findAllByOrderByCreatedAtDesc(pageRequest);
    }

    @Test
    @DisplayName("강좌 목록 조회 - 수강생 많은 순으로 정렬")
    void getCourses_SortByPopular_ShouldReturnSortedCourses() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 3);
        // 수강생 수에 따라 정렬된 결과를 시뮬레이션합니다.
        Page<Course> coursePage = new PageImpl<>(Arrays.asList(
                courses.get(4), courses.get(3), courses.get(2)
        ), pageRequest, courses.size());

        given(courseRepository.findAllByOrderByCurrentEnrollmentCountDesc(pageRequest)).willReturn(coursePage);

        // when
        Page<Course> result = courseService.getCourses(0, 3, "popular");

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Docker and Kubernetes");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Microservices Architecture");
        assertThat(result.getContent().get(2).getName()).isEqualTo("JPA Mastery");
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);

        verify(courseRepository).findAllByOrderByCurrentEnrollmentCountDesc(pageRequest);
    }

    @Test
    @DisplayName("강좌 목록 조회 - 수강률 높은 순으로 정렬")
    void getCourses_SortByHighEnrollmentRate_ShouldReturnSortedCourses() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 3);
        // 수강률에 따라 정렬된 결과를 시뮬레이션합니다.
        Page<Course> coursePage = new PageImpl<>(Arrays.asList(
                courses.get(0), courses.get(1), courses.get(2)
        ), pageRequest, courses.size());

        given(courseRepository.findAllOrderByEnrollmentRateDesc(pageRequest)).willReturn(coursePage);

        // when
        Page<Course> result = courseService.getCourses(0, 3, "highEnrollmentRate");

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Java Basic");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Spring Framework");
        assertThat(result.getContent().get(2).getName()).isEqualTo("JPA Mastery");
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);

        verify(courseRepository).findAllOrderByEnrollmentRateDesc(pageRequest);
    }

    @Test
    @DisplayName("강좌 상세 조회")
    void getCourseById_WithExistingId_ShouldReturnCourse() {
        // given
        given(courseRepository.findById(1L)).willReturn(Optional.of(course));

        // when
        Course foundCourse = courseService.getCourseById(1L);

        // then
        assertThat(foundCourse).isEqualTo(course);

        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("강좌 상세 조회 - 존재하지 않는 ID로 조회 시 EntityNotFoundException을 던진다")
    void getCourseById_WithNonExistingId_ShouldThrowEntityNotFoundException() {
        // given
        given(courseRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> courseService.getCourseById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("해당 강좌를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("강사의 강좌 목록 조회")
    void getInstructorCourses_WithExistingInstructorId_ShouldReturnCourses() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(instructor));
        given(courseRepository.findByInstructor(instructor)).willReturn(courses);

        // when
        List<Course> instructorCourses = courseService.getInstructorCourses(1L);

        // then
        assertThat(instructorCourses).hasSize(5);
        assertThat(instructorCourses).containsExactlyElementsOf(courses);

        verify(memberRepository).findById(1L);
        verify(courseRepository).findByInstructor(instructor);
    }

    @Test
    @DisplayName("학생이 수강 중인 강좌 목록 조회")
    void getStudentEnrolledCourses_WithExistingStudentId_ShouldReturnCourses() {
        // given
        Member student = new Member("Keeyong", "ky@example.com", "01087654321", "Pass1!", MemberType.STUDENT);
        Enrollment enrollment = new Enrollment(student, course);
        given(memberRepository.findById(2L)).willReturn(Optional.of(student));
        given(enrollmentRepository.findByStudent(student)).willReturn(List.of(enrollment));

        // when
        List<Course> enrolledCourses = courseService.getStudentEnrolledCourses(2L);

        // then
        assertThat(enrolledCourses).hasSize(1);
        assertThat(enrolledCourses.get(0)).isEqualTo(course);

        verify(memberRepository).findById(2L);
        verify(enrollmentRepository).findByStudent(student);
    }

}
