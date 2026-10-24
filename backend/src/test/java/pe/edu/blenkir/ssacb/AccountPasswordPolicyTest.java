package pe.edu.blenkir.ssacb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountPasswordPolicyTest {
    @Test void rejectsMissingOrShortPassword(){
        assertThrows(IllegalArgumentException.class,()->AccountPasswordPolicy.requireInitial(null));
        assertThrows(IllegalArgumentException.class,()->AccountPasswordPolicy.requireInitial("weak"));
    }
    @Test void acceptsLongPassword(){
        assertEquals("Blenkir*2026!",AccountPasswordPolicy.requireInitial("Blenkir*2026!"));
    }
}
