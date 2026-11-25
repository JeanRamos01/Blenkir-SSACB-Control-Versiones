package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UserAccountAdminController {
    private final UserAccountRepository users;
    private final StudentRepository students;
    private final TeacherRepository teachers;
    private final GuardianRepository guardians;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public UserAccountAdminController(UserAccountRepository users, StudentRepository students,
            TeacherRepository teachers, GuardianRepository guardians, PasswordEncoder encoder, AuditService audit) {
        this.users = users;
        this.students = students;
        this.teachers = teachers;
        this.guardians = guardians;
        this.encoder = encoder;
        this.audit = audit;
    }

    public record UserDto(Long id, String username, String displayName, Role role, boolean activo, String referencia) {}
    public record PasswordReset(@NotBlank @Size(min = 12, max = 128) String password) {}

    @GetMapping
    @Transactional(readOnly = true)
    public List<UserDto> list(@RequestParam(required = false) String q) {
        String query = q == null ? "" : q.strip().toLowerCase(Locale.ROOT);
        return users.findAll().stream().map(this::dto)
                .filter(user -> query.isEmpty() || (user.username() + " " + user.displayName() + " "
                        + user.role() + " " + user.referencia()).toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    @PatchMapping("/{id}/password")
    @Transactional
    public UserDto resetPassword(@PathVariable Long id, @Valid @RequestBody PasswordReset request,
            Authentication authentication) {
        UserAccount user = users.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("La contraseña no debe superar 72 bytes para el algoritmo BCrypt.");
        user.setPasswordHash(encoder.encode(request.password()));
        UserAccount saved = users.save(user);
        audit.log(authentication.getName(), "RESTABLECER_PASSWORD", "USUARIO", saved.getId());
        return dto(saved);
    }

    private UserDto dto(UserAccount user) {
        String reference = students.findByUser_Username(user.getUsername())
                .map(student -> student.getCodigo() + " · DNI " + student.getDni())
                .orElseGet(() -> teachers.findByUser_Username(user.getUsername())
                        .map(teacher -> teacher.getCodigo() + " · DNI " + teacher.getDni())
                        .orElseGet(() -> guardians.findByUser_Username(user.getUsername())
                                .map(guardian -> "DNI " + guardian.getDni()).orElse("")));
        return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(),
                user.getRole(), user.isActivo(), reference);
    }
}
