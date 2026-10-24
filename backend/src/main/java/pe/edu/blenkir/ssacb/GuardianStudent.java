package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="apoderado_estudiante", uniqueConstraints=@UniqueConstraint(columnNames={"apoderado_id","estudiante_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuardianStudent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="apoderado_id") private Guardian guardian;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="estudiante_id") private Student student;
    @Column(nullable=false, length=40) private String parentesco;
    @Column(nullable=false) @Builder.Default private boolean autorizado = true;
}
