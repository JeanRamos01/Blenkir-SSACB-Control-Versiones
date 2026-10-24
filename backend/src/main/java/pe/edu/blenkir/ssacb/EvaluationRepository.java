package pe.edu.blenkir.ssacb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EvaluationRepository extends JpaRepository<Evaluation,Long>{ List<Evaluation> findByCourse_IdOrderByFechaDesc(Long courseId); List<Evaluation> findByCourse_Teacher_IdOrderByFechaDesc(Long teacherId); }
