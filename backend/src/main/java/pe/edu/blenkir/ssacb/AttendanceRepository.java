package pe.edu.blenkir.ssacb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface AttendanceRepository extends JpaRepository<Attendance,Long>{ List<Attendance> findByStudent_IdOrderByFechaDesc(Long studentId); List<Attendance> findByCourse_IdOrderByFechaDesc(Long courseId); Optional<Attendance> findByCourse_IdAndStudent_IdAndFecha(Long courseId,Long studentId,LocalDate fecha); }
