package wb.wolbu.service;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import wb.wolbu.entity.Course;
import wb.wolbu.entity.Enrollment;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.exception.custom.BusinessLogicException;
import wb.wolbu.repository.CourseRepository;
import wb.wolbu.repository.EnrollmentRepository;
import wb.wolbu.repository.MemberRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Member student;
    private Member instructor;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        student = new Member(
                "Mark Han",
                "markymark331@gmail.com",
                "01012345678",
                "Password1!",
                MemberType.STUDENT);
        instructor = new Member(
                "Instructor",
                "example@gmail.com",
                "01015245678",
                "Password1!",
                MemberType.INSTRUCTOR);
        course = new Course(
                "Test Course",
                10,
                10000,
                instructor);
        enrollment = new Enrollment(student, course);
    }

    @Test
    @DisplayName("수강 신청 - 정상적인 경우")
    void enrollCourse_WithValidInput_ShouldEnrollSuccessfully() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(student));
        given(courseRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(course));
        given(enrollmentRepository.findByStudentAndCourse(student, course)).willReturn(Optional.empty());
        given(enrollmentRepository.save(any(Enrollment.class))).willReturn(enrollment);

        // when
        Enrollment result = enrollmentService.enrollCourse(1L, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getCourse()).isEqualTo(course);
        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("수강 신청 - 이미 신청한 강좌인 경우")
    void enrollCourse_WithAlreadyEnrolledCourse_ShouldThrowException() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(student));
        given(courseRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(course));
        given(enrollmentRepository.findByStudentAndCourse(student, course)).willReturn(Optional.of(enrollment));

        // when & then
        assertThatThrownBy(() -> enrollmentService.enrollCourse(1L, 1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("이미 수강 중인 강좌입니다.");
    }

    @Test
    @DisplayName("수강 신청 - 수강 인원 초과의 경우")
    void enrollCourse_WithFullCourse_ShouldThrowException() {
        // given
        Course fullCourse = new Course("Full Course", 1, 100000, instructor);
        fullCourse.addEnrollment(new Enrollment(new Member("Other Student", "other@example.com", "01011112222", "password", MemberType.STUDENT), fullCourse));

        given(memberRepository.findById(1L)).willReturn(Optional.of(student));
        given(courseRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(fullCourse));
        given(enrollmentRepository.findByStudentAndCourse(student, fullCourse)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> enrollmentService.enrollCourse(1L, 1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("수강 신청 인원이 꽉 찼습니다.");
    }


    @Test
    @DisplayName("수강 취소 - 정상적인 경우")
    void cancelEnrollment_WithValidInput_ShouldCancelSuccessfully() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(student));
        given(courseRepository.findById(1L)).willReturn(Optional.of(course));
        given(enrollmentRepository.findByStudentAndCourse(student, course)).willReturn(Optional.of(enrollment));

        // when
        Enrollment result = enrollmentService.cancelEnrollment(1L, 1L);

        // then
        assertThat(result).isNotNull();
        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    @DisplayName("수강 취소 - 신청하지 않은 강좌인 경우")
    void cancelEnrollment_WithNotEnrolledCourse_ShouldThrowException() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(student));
        given(courseRepository.findById(1L)).willReturn(Optional.of(course));
        given(enrollmentRepository.findByStudentAndCourse(student, course)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(1L, 1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("해당 강좌를 수강 중이 아닙니다.");
    }

    @Test
    @DisplayName("학생의 수강 목록 조회")
    void getStudentEnrollments_ShouldReturnEnrollmentList() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(student));
        given(enrollmentRepository.findByStudent(student)).willReturn(Arrays.asList(enrollment));

        // when
        List<Enrollment> results = enrollmentService.getStudentEnrollments(1L);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(enrollment);
    }

    @Test
    @DisplayName("강좌의 수강생 목록 조회")
    void getCourseEnrollments_ShouldReturnEnrollmentList() {
        // given
        given(courseRepository.findById(1L)).willReturn(Optional.of(course));
        given(enrollmentRepository.findByCourse(course)).willReturn(Arrays.asList(enrollment));

        // when
        List<Enrollment> results = enrollmentService.getCourseEnrollments(1L);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(enrollment);
    }
}