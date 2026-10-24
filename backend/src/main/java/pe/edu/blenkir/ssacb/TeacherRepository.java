package pe.edu.blenkir.ssacb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface TeacherRepository extends JpaRepository<Teacher,Long>{ Optional<Teacher> findByUser_Username(String username); Optional<Teacher> findByDni(String dni); long countByActivoTrue(); }
