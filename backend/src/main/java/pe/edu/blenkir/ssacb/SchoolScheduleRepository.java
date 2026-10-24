package pe.edu.blenkir.ssacb;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SchoolScheduleRepository extends JpaRepository<SchoolSchedule,Long> {
    List<SchoolSchedule> findByCourse_IdInOrderByDiaSemanaAscHoraInicioAsc(List<Long> courseIds);
    List<SchoolSchedule> findAllByOrderByDiaSemanaAscHoraInicioAsc();
}
