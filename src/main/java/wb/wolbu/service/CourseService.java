package wb.wolbu.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wb.wolbu.dto.UpdateCourseRequest;
import wb.wolbu.entity.Course;
import wb.wolbu.entity.Enrollment;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.exception.custom.BusinessLogicException;
import wb.wolbu.repository.CourseRepository;
import wb.wolbu.repository.EnrollmentRepository;
import wb.wolbu.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final MemberRepository memberRepository;
    private final EnrollmentRepository enrollmentRepository;

    public Course createCourse(Long instructorId, String name, Integer maxStudents, Integer price) {
        Member instructor = memberRepository.findById(instructorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + instructorId));

        if (instructor.getMemberType() != MemberType.INSTRUCTOR){
            throw new BusinessLogicException("강사만 강좌를 생성할 수 있습니다.");
        }

        Course course = new Course(name, maxStudents, price, instructor);
        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public Page<Course> getCourses(int page, int size, String sortBy){
        PageRequest pageRequest = PageRequest.of(page, size);

        return switch (sortBy) {
            case "recent" -> courseRepository.findAllByOrderByCreatedAtDesc(pageRequest);
            case "popular" -> courseRepository.findAllByOrderByCurrentEnrollmentCountDesc(pageRequest);
            case "highEnrollmentRate" -> courseRepository.findAllOrderByEnrollmentRateDesc(pageRequest);
            default -> throw new IllegalArgumentException("Invalid sortBy parameter: " + sortBy);
        };
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long courseId){
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강좌를 찾을 수 없습니다. id=" + courseId));
    }

    @Transactional(readOnly = true)
    public List<Course> getInstructorCourses(Long instructorId){
        Member instructor = memberRepository.findById(instructorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + instructorId));

        return courseRepository.findByInstructor(instructor);
    }

    @Transactional(readOnly = true)
    public List<Course> getStudentEnrolledCourses(Long studentId) {
        Member student = memberRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 학생을 찾을 수 없습니다. id=" + studentId));

        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
        return enrollments.stream()
                .map(Enrollment::getCourse)
                .toList();
    }

    public Course updateCourse(Long courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강좌를 찾을 수 없습니다. id=" + courseId));

        // TODO: 권한 체크 필요

        course.update(request.getName(), request.getMaxStudents(), request.getPrice());
        return courseRepository.save(course);
    }

    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강좌를 찾을 수 없습니다. id=" + courseId));
        // TODO: 권한 체크 필요
        courseRepository.delete(course);
    }
}
