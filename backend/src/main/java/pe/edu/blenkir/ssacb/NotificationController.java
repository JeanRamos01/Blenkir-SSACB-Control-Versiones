package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final SchoolNotificationRepository notifications;private final AuditService audit;
    public NotificationController(SchoolNotificationRepository notifications,AuditService audit){this.notifications=notifications;this.audit=audit;}
    public record SaveNotification(@NotBlank @Size(max=120) String titulo,@NotBlank @Size(max=3000) String mensaje,@NotBlank String rolDestino){}
    public record NotificationDto(Long id,String titulo,String mensaje,String rolDestino,LocalDateTime fecha,String autor){}
    static NotificationDto dto(SchoolNotification n){return new NotificationDto(n.getId(),n.getTitulo(),n.getMensaje(),n.getRolDestino(),n.getFecha(),n.getAutor());}
    @GetMapping @Transactional(readOnly=true)
    public List<NotificationDto> list(Authentication auth){
        String role=auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_","");
        return notifications.findAllByOrderByFechaDesc().stream()
           .filter(n->role.equals("ADMINISTRADOR")||n.getRolDestino().equals("TODOS")||n.getRolDestino().equals(role))
           .map(NotificationController::dto).toList();
    }
    @PostMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','SECRETARIA')") @Transactional
    public NotificationDto create(@Valid @RequestBody SaveNotification r,Authentication auth){
        if(!r.rolDestino().equals("TODOS") && Arrays.stream(Role.values()).noneMatch(role->role.name().equals(r.rolDestino())))
            throw new IllegalArgumentException("Destinatario inválido");
        var n=notifications.save(SchoolNotification.builder().titulo(r.titulo()).mensaje(r.mensaje())
          .rolDestino(r.rolDestino()).fecha(LocalDateTime.now()).autor(auth.getName()).build());
        audit.log(auth.getName(),"CREAR","COMUNICADO",n.getId());return dto(n);
    }
}
