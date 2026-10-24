package pe.edu.blenkir.ssacb;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompetencyRepository extends JpaRepository<Competency,Long> {
    List<Competency> findByCourse_Id(Long id);
}
