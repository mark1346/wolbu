package wb.wolbu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import wb.wolbu.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // 강의를 최근 등록 순으로 정렬
    Page<Course> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 강의를 신청자 많은 순으로 정렬
    Page<Course> findAllByOrderByCurrentEnrollmentCountDesc(Pageable pageable);

    // 강의를 신청률 높은 순으로 정렬
    @Query("SELECT c FROM Course c ORDER BY (c.currentEnrollmentCount * 1.0 / c.maxStudents) DESC")
    Page<Course> findAllOrderByEnrollmentRateDesc(Pageable pageable);
}
