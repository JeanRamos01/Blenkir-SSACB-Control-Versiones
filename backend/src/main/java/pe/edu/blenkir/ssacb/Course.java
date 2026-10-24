package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="cursos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=30) private String codigo;
    @Column(nullable=false, length=120) private String nombre;
    @Column(length=40) private String grado;
    @Column(length=10) private String seccion;
    @Column(length=30) private String periodo;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="docente_id") private Teacher teacher;
    @Column(nullable=false) @Builder.Default private boolean activo = true;
}
