package pe.edu.blenkir.ssacb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CourseRepository extends JpaRepository<Course,Long>{ List<Course> findByTeacher_IdAndActivoTrue(Long teacherId); long countByActivoTrue(); }
