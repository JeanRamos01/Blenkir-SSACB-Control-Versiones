package pe.edu.blenkir.ssacb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EnrollmentRepository extends JpaRepository<Enrollment,Long>{ List<Enrollment> findByCourse_IdAndActivoTrue(Long courseId); List<Enrollment> findByStudent_IdAndActivoTrue(Long studentId); Optional<Enrollment> findByStudent_IdAndCourse_Id(Long studentId,Long courseId); }
