package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="competencias_blenkir")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Competency {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="curso_id") private Course course;
    @Column(nullable=false,length=200) private String nombre;
    @Column(length=800) private String descripcion;
}
