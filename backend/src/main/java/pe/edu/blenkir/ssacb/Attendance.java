package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name="asistencias", uniqueConstraints=@UniqueConstraint(columnNames={"curso_id","estudiante_id","fecha"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {
    public enum Estado { PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="curso_id") private Course course;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="estudiante_id") private Student student;
    @Column(nullable=false) private LocalDate fecha;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private Estado estado;
    @Column(length=240) private String observacion;
}
