package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="cuentas_por_cobrar",uniqueConstraints=@UniqueConstraint(columnNames={"estudiante_id","concepto_id","periodo"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentCharge {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="estudiante_id") private Student student;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="concepto_id") private PaymentConcept concept;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal importe;
    @Column(nullable=false,precision=12,scale=2) @Builder.Default private BigDecimal pagado=BigDecimal.ZERO;
    @Column(nullable=false) private LocalDate vencimiento;
    @Column(nullable=false,length=50) private String periodo;
    @Version private Long version;
}
