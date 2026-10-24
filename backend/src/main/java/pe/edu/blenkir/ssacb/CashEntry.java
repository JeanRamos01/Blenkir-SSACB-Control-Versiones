package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="movimientos_caja_blenkir")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CashEntry {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private LocalDateTime fecha;
    @Column(nullable=false,length=10) private String tipo;
    @Column(nullable=false,length=160) private String concepto;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal importe;
    @Column(nullable=false,length=80) private String usuario;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="pago_id",unique=true) private StudentPayment payment;
}
