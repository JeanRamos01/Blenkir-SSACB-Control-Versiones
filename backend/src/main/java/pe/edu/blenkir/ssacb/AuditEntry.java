package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="auditoria_blenkir")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditEntry {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private LocalDateTime fecha;
    @Column(nullable=false,length=80) private String usuario;
    @Column(nullable=false,length=60) private String accion;
    @Column(nullable=false,length=60) private String entidad;
    @Column(nullable=false,length=80) private String referencia;
}
