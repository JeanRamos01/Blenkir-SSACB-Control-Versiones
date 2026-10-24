package pe.edu.blenkir.ssacb;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CompetencyResultRepository extends JpaRepository<CompetencyResult,Long> {
    Optional<CompetencyResult> findByCompetency_IdAndStudent_IdAndPeriodo(Long competencyId,Long studentId,String periodo);
}
