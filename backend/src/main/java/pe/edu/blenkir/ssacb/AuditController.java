package pe.edu.blenkir.ssacb;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditController {
    private final AuditEntryRepository entries;
    public AuditController(AuditEntryRepository entries){this.entries=entries;}
    @GetMapping
    public List<AuditEntry> list(){return entries.findTop200ByOrderByFechaDesc();}
}
