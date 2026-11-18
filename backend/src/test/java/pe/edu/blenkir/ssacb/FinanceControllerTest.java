package pe.edu.blenkir.ssacb;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanceControllerTest {
    private final PaymentConceptRepository concepts=mock(PaymentConceptRepository.class);
    private final StudentChargeRepository charges=mock(StudentChargeRepository.class);
    private final List<StudentPayment> savedPayments=new ArrayList<>();
    private final StudentPaymentRepository payments=mock(StudentPaymentRepository.class,invocation->{
        if("save".equals(invocation.getMethod().getName())){
            StudentPayment payment=Objects.requireNonNull(invocation.getArgument(0));
            payment.setId(21L);
            savedPayments.add(payment);
            return payment;
        }
        return RETURNS_DEFAULTS.answer(invocation);
    });
    private final List<CashEntry> savedCashEntries=new ArrayList<>();
    private final CashEntryRepository cash=mock(CashEntryRepository.class,invocation->{
        if("save".equals(invocation.getMethod().getName())){
            CashEntry entry=Objects.requireNonNull(invocation.getArgument(0));
            savedCashEntries.add(entry);
            return entry;
        }
        return RETURNS_DEFAULTS.answer(invocation);
    });
    private final StudentRepository students=mock(StudentRepository.class);
    private final AuditService audit=mock(AuditService.class);
    private final FinanceController controller=new FinanceController(concepts,charges,payments,cash,students,audit);
    private final UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken("secretaria",null);

    private StudentCharge pending(){
        return StudentCharge.builder().id(5L).importe(new BigDecimal("50.00"))
            .pagado(BigDecimal.ZERO).periodo("2026-10").vencimiento(LocalDate.now().plusDays(10))
            .concept(PaymentConcept.builder().id(2L).nombre("Pensión").importe(new BigDecimal("50.00")).build())
            .student(Student.builder().id(3L).nombres("Alumno").apellidos("Blenkir").build()).build();
    }

    @Test void rejectsPaymentsAboveBalanceWithoutChangingCash(){
        when(charges.lockById(5L)).thenReturn(Optional.of(pending()));
        var request=new FinanceController.PaymentInput(5L,new BigDecimal("50.01"),"EFECTIVO",null);
        assertThrows(IllegalArgumentException.class,()->controller.pay(request,auth));
        verifyNoInteractions(payments,cash);
    }

    @Test void recordsPaymentAndCashInSameOperation(){
        var item=pending();when(charges.lockById(5L)).thenReturn(Optional.of(item));
        var receipt=controller.pay(new FinanceController.PaymentInput(5L,new BigDecimal("20.00"),"YAPE","OP123"),auth);
        assertEquals(new BigDecimal("20.00"),receipt.importe());
        assertEquals(new BigDecimal("20.00"),item.getPagado());
        assertEquals(1,savedPayments.size());
        assertEquals(21L,savedPayments.get(0).getId());
        assertEquals(1,savedCashEntries.size());
        var entry=savedCashEntries.get(0);
        assertEquals(21L,entry.getPayment().getId());
        assertEquals("INGRESO",entry.getTipo());
        assertEquals(0,new BigDecimal("20.00").compareTo(entry.getImporte()));
    }
}
