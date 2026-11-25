package pe.edu.blenkir.ssacb;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface AcademicRiskAlertRepository extends JpaRepository<AcademicRiskAlert, Long> {
    boolean existsByStudent_IdAndCourse_IdAndPeriodoAndEstadoNot(
            Long studentId, Long courseId, String periodo, String estado);
    List<AcademicRiskAlert> findAllByOrderByFechaGeneracionDesc();
    List<AcademicRiskAlert> findByCourse_Teacher_IdOrderByFechaGeneracionDesc(Long teacherId);
    List<AcademicRiskAlert> findByStudent_IdInOrderByFechaGeneracionDesc(Collection<Long> studentIds);
    List<AcademicRiskAlert> findByStudent_IdOrderByFechaGeneracionDesc(Long studentId);
}
