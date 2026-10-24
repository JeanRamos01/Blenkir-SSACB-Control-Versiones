package pe.edu.blenkir.ssacb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GuardianStudentRepository extends JpaRepository<GuardianStudent,Long>{ List<GuardianStudent> findByGuardian_IdAndAutorizadoTrue(Long guardianId); Optional<GuardianStudent> findByGuardian_IdAndStudent_DniAndAutorizadoTrue(Long guardianId,String dni); Optional<GuardianStudent> findByGuardian_IdAndStudent_IdAndAutorizadoTrue(Long guardianId,Long studentId); }
