package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name="evaluaciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Evaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="curso_id") private Course course;
    @Column(nullable=false, length=140) private String nombre;
    @Column(length=60) private String tipo;
    @Column(nullable=false) private LocalDate fecha;
    @Column(nullable=false) @Builder.Default private Integer peso = 100;
}
