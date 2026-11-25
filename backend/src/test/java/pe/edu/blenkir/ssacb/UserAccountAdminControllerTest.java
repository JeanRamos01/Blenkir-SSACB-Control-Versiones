package pe.edu.blenkir.ssacb;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAccountAdminControllerTest {
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final StudentRepository students = mock(StudentRepository.class);
    private final TeacherRepository teachers = mock(TeacherRepository.class);
    private final GuardianRepository guardians = mock(GuardianRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuditService audit = mock(AuditService.class);
    private final UserAccountAdminController controller =
            new UserAccountAdminController(users, students, teachers, guardians, encoder, audit);

    @Test
    void resetsUsingTheConfiguredEncoderAndDoesNotReturnPassword() {
        UserAccount user = UserAccount.builder().id(7L).username("student-demo")
                .displayName("Estudiante DEMO").role(Role.ESTUDIANTE).activo(true)
                .passwordHash("old-hash").build();
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(encoder.encode("NewSecurePassword123!")).thenReturn("bcrypt-hash");
        when(users.save(user)).thenReturn(user);
        when(students.findByUser_Username("student-demo")).thenReturn(Optional.empty());
        when(teachers.findByUser_Username("student-demo")).thenReturn(Optional.empty());
        when(guardians.findByUser_Username("student-demo")).thenReturn(Optional.empty());
        var auth = new UsernamePasswordAuthenticationToken("admin", null);

        var result = controller.resetPassword(7L,
                new UserAccountAdminController.PasswordReset("NewSecurePassword123!"), auth);

        assertEquals("bcrypt-hash", user.getPasswordHash());
        assertEquals("student-demo", result.username());
        assertFalse(result.toString().contains("NewSecurePassword123!"));
        verify(audit).log("admin", "RESTABLECER_PASSWORD", "USUARIO", 7L);
    }

    @Test
    void endpointIsRestrictedToAdministrators() {
        var authorization = UserAccountAdminController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(authorization);
        assertEquals("hasRole('ADMINISTRADOR')", authorization.value());
    }
}
