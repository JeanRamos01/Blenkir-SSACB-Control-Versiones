package pe.edu.blenkir.ssacb;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="conceptos_cobro_blenkir")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentConcept {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=70) private String nombre;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal importe;
    @Column(nullable=false) @Builder.Default private boolean activo=true;
}
