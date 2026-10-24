package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="logros_competencias_blenkir",uniqueConstraints=@UniqueConstraint(columnNames={"competencia_id","estudiante_id","periodo"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompetencyResult {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="competencia_id") private Competency competency;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="estudiante_id") private Student student;
    @Column(nullable=false,length=30) private String periodo;
    @Column(nullable=false,length=2) private String nivel;
    @Column(length=1000) private String conclusion;
}
