package wb.wolbu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wb.wolbu.entity.Course;
import wb.wolbu.entity.Enrollment;
import wb.wolbu.entity.Member;
import wb.wolbu.entity.MemberType;
import wb.wolbu.repository.CourseRepository;
import wb.wolbu.repository.EnrollmentRepository;
import wb.wolbu.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
public class EnrollmentService {
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
    
}