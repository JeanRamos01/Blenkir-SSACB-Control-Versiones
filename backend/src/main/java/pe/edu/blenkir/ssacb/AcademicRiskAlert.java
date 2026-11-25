package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_academicas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AcademicRiskAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 30)
    private String periodo;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false)
    private Double promedio;

    @Column(nullable = false)
    private Double umbral;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @PrePersist
    void stamp() {
        if (fechaGeneracion == null) fechaGeneracion = LocalDateTime.now();
    }
}
