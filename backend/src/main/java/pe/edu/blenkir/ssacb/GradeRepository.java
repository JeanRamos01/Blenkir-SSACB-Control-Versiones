package pe.edu.blenkir.ssacb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GradeRepository extends JpaRepository<Grade,Long>{ List<Grade> findByStudent_IdOrderByEvaluation_FechaDesc(Long studentId); List<Grade> findByEvaluation_Course_IdOrderByEvaluation_FechaDesc(Long courseId); Optional<Grade> findByEvaluation_IdAndStudent_Id(Long evaluationId,Long studentId); long count(); }
