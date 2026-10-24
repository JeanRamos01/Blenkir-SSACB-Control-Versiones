package pe.edu.blenkir.ssacb;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface GuardianRepository extends JpaRepository<Guardian,Long>{ Optional<Guardian> findByUser_Username(String username); Optional<Guardian> findByDni(String dni); }
