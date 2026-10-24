package pe.edu.blenkir.ssacb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student,Long>{ Optional<Student> findByDni(String dni); Optional<Student> findByUser_Username(String username); long countByActivoTrue(); }
