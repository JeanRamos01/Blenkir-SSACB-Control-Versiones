package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personal_colegio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SchoolStaff {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=8, unique=true) private String dni;
    @Column(nullable=false, length=160) private String nombreCompleto;
    @Column(nullable=false, length=50) private String cargo;
    @Column(length=120) private String correo;
    @Column(length=20) private String telefono;
    @Column(nullable=false) @Builder.Default private boolean activo=true;
}
