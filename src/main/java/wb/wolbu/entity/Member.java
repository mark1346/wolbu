package wb.wolbu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType memberType;

    // 강사가 가르치는 강의 set
    @OneToMany(mappedBy = "instructor")
    private Set<Course> instructedCourses = new HashSet<>();

    // 학생이 등록한 강의 set
    @OneToMany(mappedBy = "student")
    private Set<Enrollment> enrollments = new HashSet<>();

    public Member(String name, String phoneNumber, String password, MemberType memberType) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.memberType = memberType;
    }


    // 비밀번호 변경 메서드
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // 강의 추가 메서드 (강사용)
    public void addInstructedCourse(Course course) {
        this.instructedCourses.add(course);
        course.setInstructor(this);
    }

    // 수강 신청 메서드 (학생용)
    public void enrollCourse(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setStudent(this);
    }
}
