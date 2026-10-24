package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name="horarios_clases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SchoolSchedule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="curso_id") private Course course;
    @Column(nullable=false) private Integer diaSemana;
    @Column(nullable=false) private LocalTime horaInicio;
    @Column(nullable=false) private LocalTime horaFin;
    @Column(length=80) private String aula;
}
