package wb.wolbu.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wb.wolbu.entity.Course;
import wb.wolbu.entity.Enrollment;
import wb.wolbu.entity.Member;
import wb.wolbu.exception.custom.BusinessLogicException;
import wb.wolbu.repository.CourseRepository;
import wb.wolbu.repository.EnrollmentRepository;
import wb.wolbu.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final MemberRepository memberRepository;

    public Enrollment enrollCourse(Long studentId, Long courseId) {
        Member student = memberRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 학생을 찾을 수 없습니다. id=" + studentId));

        int maxRetry = 3; // 최대 재시도 횟수
        int retryCount = 0; // 현재 재시도 횟수

        while (retryCount < maxRetry) {
            try {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new EntityNotFoundException("해당 강좌를 찾을 수 없습니다. id=" + courseId));

                if (enrollmentRepository.findByStudentAndCourse(student, course).isPresent()) {
                    throw new BusinessLogicException("이미 수강 중인 강좌입니다.");
                }

                if (!course.canEnroll()) {
                    throw new BusinessLogicException("수강 신청 인원이 꽉 찼습니다.");
                }

                Enrollment enrollment = new Enrollment(student, course);
                course.addEnrollment(enrollment);
                enrollmentRepository.save(enrollment);
                courseRepository.save(course);

                return enrollment; // 성공하면 메소드 종료!
            } catch (OptimisticLockException e) {
                retryCount++;
                if (retryCount >= maxRetry) {
                    throw new BusinessLogicException("수강 신청 처리 중 반복적인 충돌이 발생했습니다. 잠시 후 다시 시도해주세요.");
                }
                // 짧은 대기시간 추가
                try {
                    Thread.sleep(100 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessLogicException("수간 신청 처리가 중단되었습니다.");
                }
            }
        }
        // 최대 재시도 횟수를 초과했을 때. 흠 컴파일 오륜가?
        throw new BusinessLogicException("수강 신청 처리에 실패했습니다. 잠시 후에 다시 시도해주세요");
    }

    public Enrollment cancelEnrollment(Long studentId, Long courseId) {
        Member student = memberRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 학생을 찾을 수 없습니다. id=" + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강좌를 찾을 수 없습니다. id=" + courseId));

        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new BusinessLogicException("해당 강좌를 수강 중이 아닙니다."));

        course.removeEnrollment(enrollment);
        enrollmentRepository.delete(enrollment);

        return enrollment;
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getStudentEnrollments(Long studentId) {
        Member student = memberRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 학생을 찾을 수 없습니다. id=" + studentId));

        return enrollmentRepository.findByStudent(student);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getCourseEnrollments(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강좌를 찾을 수 없습니다. id=" + courseId));

        return enrollmentRepository.findByCourse(course);
    }

}
