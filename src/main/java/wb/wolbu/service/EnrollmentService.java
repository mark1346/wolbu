package wb.wolbu.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import wb.wolbu.dto.EnrollmentResult;
import wb.wolbu.entity.Course;
import wb.wolbu.entity.Enrollment;
import wb.wolbu.entity.Member;
import wb.wolbu.exception.custom.BusinessLogicException;
import wb.wolbu.repository.CourseRepository;
import wb.wolbu.repository.EnrollmentRepository;
import wb.wolbu.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Enrollment enrollCourse(Long studentId, Long courseId) {
        try {
            Member student = memberRepository.findById(studentId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 학생을 찾을 수 없습니다. id=" + studentId));

            Course course = courseRepository.findByIdWithPessimisticLock(courseId)
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

            return enrollment;

        } catch (Exception e) {
            System.out.println("This is e: "+e.getMessage());
            throw e;
        }
    }

    public List<EnrollmentResult> enrollMultipleCourses(Long studentId, List<Long> courseIds) {
        Member student = memberRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 학생을 찾을 수 없습니다. id=" + studentId));

        List<EnrollmentResult> results = new ArrayList<>();

        for (Long courseId : courseIds){
            try {
                Enrollment enrollment = enrollCourse(studentId, courseId);
                results.add(new EnrollmentResult(courseId, true, null));
            } catch (BusinessLogicException e) {
                results.add(new EnrollmentResult(courseId, false, e.getMessage()));
            }
        }

        return results;
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
