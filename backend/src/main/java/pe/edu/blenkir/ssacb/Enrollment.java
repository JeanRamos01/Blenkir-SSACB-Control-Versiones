package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="matriculas_curso", uniqueConstraints=@UniqueConstraint(columnNames={"estudiante_id","curso_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="estudiante_id") private Student student;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="curso_id") private Course course;
    @Column(nullable=false) @Builder.Default private boolean activo = true;
}
