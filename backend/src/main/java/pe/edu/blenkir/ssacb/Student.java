package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "estudiantes", indexes = @Index(name="idx_estudiante_dni", columnList="dni"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=30) private String codigo;
    @Column(nullable=false, unique=true, length=8) private String dni;
    @Column(nullable=false, length=80) private String nombres;
    @Column(nullable=false, length=100) private String apellidos;
    private LocalDate fechaNacimiento;
    @Column(length=40) private String grado;
    @Column(length=10) private String seccion;
    @Column(length=120) private String email;
    @Column(length=200) private String direccion;
    @Column(length=20) private String telefono;
    @Column(nullable=false) @Builder.Default private boolean activo = true;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name="usuario_id", unique=true) private UserAccount user;
}
