package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/finance")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SECRETARIA')")
public class FinanceController {
    private final PaymentConceptRepository concepts;
    private final StudentChargeRepository charges;
    private final StudentPaymentRepository payments;
    private final CashEntryRepository cash;
    private final StudentRepository students;
    private final AuditService audit;
    public FinanceController(PaymentConceptRepository concepts,StudentChargeRepository charges,StudentPaymentRepository payments,
      CashEntryRepository cash,StudentRepository students,AuditService audit){
        this.concepts=concepts;this.charges=charges;this.payments=payments;this.cash=cash;this.students=students;this.audit=audit;
    }
    public record ConceptInput(@NotBlank @Size(max=70) String nombre,@NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal importe){}
    public record ChargeInput(@NotNull Long estudianteId,@NotNull Long conceptoId,@NotBlank @Size(max=50) String periodo,@NotNull LocalDate vencimiento){}
    public record PaymentInput(@NotNull Long cuentaId,@NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal importe,@NotBlank String medio,@Size(max=100) String referencia){}
    public record CashInput(@NotBlank String tipo,@NotBlank @Size(max=160) String concepto,@NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal importe){}
    public record ConceptDto(Long id,String nombre,BigDecimal importe,boolean activo){}
    public record ChargeDto(Long id,Long estudianteId,String estudiante,Long conceptoId,String concepto,String periodo,LocalDate vencimiento,BigDecimal importe,BigDecimal pagado,BigDecimal saldo,String estado){}
    public record PaymentDto(Long id,Long cuentaId,BigDecimal importe,String medio,LocalDateTime fecha,String registradoPor,String referencia){}
    public record CashDto(Long id,LocalDateTime fecha,String tipo,String concepto,BigDecimal importe,String usuario,Long pagoId){}
    public record CashSummary(BigDecimal ingresos,BigDecimal egresos,BigDecimal saldo,List<CashDto> movimientos){}
    static ConceptDto dto(PaymentConcept c){return new ConceptDto(c.getId(),c.getNombre(),c.getImporte(),c.isActivo());}
    static ChargeDto dto(StudentCharge c){var s=c.getStudent();var balance=c.getImporte().subtract(c.getPagado());
        String estado=balance.signum()==0?"PAGADO":c.getPagado().signum()>0?"PARCIAL":c.getVencimiento().isBefore(LocalDate.now())?"VENCIDO":"PENDIENTE";
        return new ChargeDto(c.getId(),s.getId(),s.getNombres()+" "+s.getApellidos(),c.getConcept().getId(),c.getConcept().getNombre(),c.getPeriodo(),c.getVencimiento(),c.getImporte(),c.getPagado(),balance,estado);
    }
    static PaymentDto dto(StudentPayment p){return new PaymentDto(p.getId(),p.getCharge().getId(),p.getImporte(),p.getMedio(),p.getFecha(),p.getRegistradoPor(),p.getReferencia());}
    static CashDto dto(CashEntry x){return new CashDto(x.getId(),x.getFecha(),x.getTipo(),x.getConcepto(),x.getImporte(),x.getUsuario(),x.getPayment()==null?null:x.getPayment().getId());}

    @GetMapping("/concepts") @Transactional(readOnly=true)
    public List<ConceptDto> concepts(){return concepts.findAll().stream().map(FinanceController::dto).toList();}

    @PostMapping("/concepts") @PreAuthorize("hasRole('ADMINISTRADOR')") @Transactional
    public ConceptDto createConcept(@Valid @RequestBody ConceptInput r,Authentication auth){
        var c=concepts.save(PaymentConcept.builder().nombre(r.nombre().trim()).importe(r.importe()).build());
        audit.log(auth.getName(),"CREAR","CONCEPTO_COBRO",c.getId());return dto(c);
    }

    @GetMapping("/charges") @Transactional(readOnly=true)
    public List<ChargeDto> charges(){return charges.findAll().stream().map(FinanceController::dto).toList();}

    @PostMapping("/charges") @Transactional
    public ChargeDto createCharge(@Valid @RequestBody ChargeInput r,Authentication auth){
        var s=students.findById(r.estudianteId()).orElseThrow(()->new IllegalArgumentException("Estudiante no encontrado"));
        var c=concepts.findById(r.conceptoId()).orElseThrow(()->new IllegalArgumentException("Concepto no encontrado"));
        if(!s.isActivo()||!c.isActivo())throw new IllegalArgumentException("Estudiante o concepto inactivo");
        var charge=charges.save(StudentCharge.builder().student(s).concept(c).importe(c.getImporte())
            .pagado(BigDecimal.ZERO).periodo(r.periodo()).vencimiento(r.vencimiento()).build());
        audit.log(auth.getName(),"CREAR","CUENTA_COBRO",charge.getId());return dto(charge);
    }

    @GetMapping("/payments") @Transactional(readOnly=true)
    public List<PaymentDto> payments(){return payments.findAll().stream().map(FinanceController::dto).toList();}

    @PostMapping("/payments") @Transactional
    public PaymentDto pay(@Valid @RequestBody PaymentInput r,Authentication auth){
        var charge=charges.lockById(r.cuentaId()).orElseThrow(()->new IllegalArgumentException("Cuenta no encontrada"));
        var amount=r.importe().setScale(2,java.math.RoundingMode.UNNECESSARY);
        if(amount.compareTo(charge.getImporte().subtract(charge.getPagado()))>0)throw new IllegalArgumentException("El pago supera el saldo pendiente");
        if(!Set.of("EFECTIVO","TRANSFERENCIA","TARJETA","YAPE","PLIN").contains(r.medio()))throw new IllegalArgumentException("Medio de pago inválido");
        charge.setPagado(charge.getPagado().add(amount));charges.save(charge);
        var payment=payments.save(StudentPayment.builder().charge(charge).importe(amount).medio(r.medio()).
             referencia(r.referencia()).registradoPor(auth.getName()).fecha(LocalDateTime.now()).build());
        cash.save(CashEntry.builder().fecha(payment.getFecha()).tipo("INGRESO").concepto("Cobro "+charge.getConcept().getNombre()).
             importe(amount).usuario(auth.getName()).payment(payment).build());
        audit.log(auth.getName(),"REGISTRAR_PAGO","PAGO",payment.getId());return dto(payment);
    }

    @GetMapping("/cash") @Transactional(readOnly=true)
    public CashSummary cash(){
        var all=cash.findAll();var income=BigDecimal.ZERO;var expense=BigDecimal.ZERO;
        for(var c:all){if(c.getTipo().equals("INGRESO"))income=income.add(c.getImporte());else expense=expense.add(c.getImporte());}
        return new CashSummary(income,expense,income.subtract(expense),all.stream().sorted(Comparator.comparing(CashEntry::getFecha).reversed()).map(FinanceController::dto).toList());
    }

    @PostMapping("/cash") @PreAuthorize("hasRole('ADMINISTRADOR')") @Transactional
    public CashDto manualEntry(@Valid @RequestBody CashInput r,Authentication auth){
        if(!Set.of("INGRESO","EGRESO").contains(r.tipo()))throw new IllegalArgumentException("Tipo de movimiento inválido");
        var entry=cash.save(CashEntry.builder().tipo(r.tipo()).concepto(r.concepto()).importe(r.importe()).
               fecha(LocalDateTime.now()).usuario(auth.getName()).build());
        audit.log(auth.getName(),"REGISTRAR_MOVIMIENTO","CAJA",entry.getId());return dto(entry);
    }
}
