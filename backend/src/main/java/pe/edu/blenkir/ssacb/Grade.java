package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="calificaciones", uniqueConstraints=@UniqueConstraint(columnNames={"evaluacion_id","estudiante_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Grade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="evaluacion_id") private Evaluation evaluation;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="estudiante_id") private Student student;
    @Column(nullable=false) private Double nota;
    @Column(length=240) private String observacion;
    @Column(nullable=false) private LocalDateTime actualizadoEn;
    @PrePersist @PreUpdate void stamp(){ actualizadoEn = LocalDateTime.now(); }
}
