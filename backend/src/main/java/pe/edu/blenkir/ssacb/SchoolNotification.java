package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="comunicados_blenkir")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SchoolNotification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=120) private String titulo;
    @Column(nullable=false,length=3000) private String mensaje;
    @Column(nullable=false,length=30) private String rolDestino;
    @Column(nullable=false) private LocalDateTime fecha;
    @Column(nullable=false,length=80) private String autor;
}
