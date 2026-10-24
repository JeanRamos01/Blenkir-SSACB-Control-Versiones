package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="pagos_blenkir")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentPayment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="cuenta_id") private StudentCharge charge;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal importe;
    @Column(nullable=false,length=30) private String medio;
    @Column(nullable=false) private LocalDateTime fecha;
    @Column(nullable=false,length=80) private String registradoPor;
    @Column(length=100) private String referencia;
}
