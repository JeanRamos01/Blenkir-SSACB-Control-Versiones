package pe.edu.blenkir.ssacb;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface StudentChargeRepository extends JpaRepository<StudentCharge,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from StudentCharge c where c.id = :id")
    Optional<StudentCharge> lockById(@Param("id") Long id);
}
