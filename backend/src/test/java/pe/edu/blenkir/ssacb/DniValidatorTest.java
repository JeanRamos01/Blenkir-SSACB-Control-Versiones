package pe.edu.blenkir.ssacb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DniValidatorTest {
    @Test
    void normalizeAndValidatesEightDigitDni() {
        assertEquals("12345678", DniValidator.requireValid(" 12345678 "));
        assertEquals(true, DniValidator.isValid("12345678"));
    }

    @Test
    void rejectsInvalidDni() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> DniValidator.requireValid("1234567"));
        assertEquals("El DNI debe tener exactamente 8 dígitos numéricos.", ex.getMessage());
    }
}
