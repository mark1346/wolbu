package wb.wolbu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wb.wolbu.exception.custom.BusinessLogicException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer maxStudents;

    @Column(nullable = false)
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private Member instructor;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    @Column(nullable = false)
    private Integer currentEnrollmentCount = 0;

    public Course(String name, Integer maxStudents, Integer price, Member instructor) {
        this.name = name;
        this.maxStudents = maxStudents;
        this.price = price;
        this.instructor = instructor;
        this.createdAt = LocalDateTime.now();
    }

    public boolean canEnroll() {
        return this.currentEnrollmentCount < this.maxStudents;
    }

    public void addEnrollment(Enrollment enrollment) {
        if (!canEnroll()) {
            throw new BusinessLogicException("수강 신청 인원이 꽉 찼습니다.");
        }
        this.enrollments.add(enrollment);
        this.currentEnrollmentCount++;
    }

    public void setInstructor(Member instructor) {
        this.instructor = instructor;
    }
}
