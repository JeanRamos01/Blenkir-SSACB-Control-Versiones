package pe.edu.blenkir.ssacb;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FinanceControllerTest {
    private final PaymentConceptRepository concepts=mock(PaymentConceptRepository.class);
    private final StudentChargeRepository charges=mock(StudentChargeRepository.class);
    private final StudentPaymentRepository payments=mock(StudentPaymentRepository.class);
    private final CashEntryRepository cash=mock(CashEntryRepository.class);
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
        verify(payments,never()).save(any());
        verify(cash,never()).save(any());
    }

    @Test void recordsPaymentAndCashInSameOperation(){
        var item=pending();when(charges.lockById(5L)).thenReturn(Optional.of(item));
        when(payments.save(any())).thenAnswer(invocation->{StudentPayment p=invocation.getArgument(0);p.setId(21L);return p;});
        var receipt=controller.pay(new FinanceController.PaymentInput(5L,new BigDecimal("20.00"),"YAPE","OP123"),auth);
        assertEquals(new BigDecimal("20.00"),receipt.importe());
        assertEquals(new BigDecimal("20.00"),item.getPagado());
        verify(cash).save(argThat(entry->entry.getPayment().getId()==21L && entry.getTipo().equals("INGRESO")
            && entry.getImporte().compareTo(new BigDecimal("20.00"))==0));
    }
}
