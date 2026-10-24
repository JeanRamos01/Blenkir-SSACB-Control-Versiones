package pe.edu.blenkir.ssacb;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuditService {
    private final AuditEntryRepository entries;
    public AuditService(AuditEntryRepository entries){this.entries=entries;}
    public void log(String user,String action,String entity,Object id){
        entries.save(AuditEntry.builder().fecha(LocalDateTime.now()).usuario(user).accion(action)
          .entidad(entity).referencia(String.valueOf(id)).build());
    }
}
