package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "docentes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Teacher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=30) private String codigo;
    @Column(nullable=false, unique=true, length=8) private String dni;
    @Column(nullable=false, length=80) private String nombres;
    @Column(nullable=false, length=100) private String apellidos;
    @Column(length=120) private String email;
    @Column(length=100) private String especialidad;
    @Column(nullable=false) @Builder.Default private boolean activo = true;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name="usuario_id", unique=true) private UserAccount user;
}
