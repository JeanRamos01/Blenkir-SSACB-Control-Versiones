package pe.edu.blenkir.ssacb;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SchoolNotificationRepository extends JpaRepository<SchoolNotification,Long> {
    List<SchoolNotification> findAllByOrderByFechaDesc();
}
