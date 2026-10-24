package pe.edu.blenkir.ssacb;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditEntryRepository extends JpaRepository<AuditEntry,Long> {
    List<AuditEntry> findTop200ByOrderByFechaDesc();
}
