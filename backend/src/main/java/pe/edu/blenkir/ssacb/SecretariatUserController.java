package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/secretariat-users")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class SecretariatUserController {
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final AuditService audit;
    public SecretariatUserController(UserAccountRepository users,PasswordEncoder encoder,AuditService audit){this.users=users;this.encoder=encoder;this.audit=audit;}

    public record UserRequest(@NotBlank @Size(max=80) String username,@NotBlank @Size(max=160) String displayName,
      @NotBlank @Size(min=12) String password){}
    public record UserDto(Long id,String username,String displayName,boolean activo){}
    static UserDto dto(UserAccount user){return new UserDto(user.getId(),user.getUsername(),user.getDisplayName(),user.isActivo());}

    @GetMapping @Transactional(readOnly=true)
    public List<UserDto> list(){return users.findAll().stream().filter(u->u.getRole()==Role.SECRETARIA).map(SecretariatUserController::dto).toList();}

    @PostMapping @Transactional
    public UserDto create(@Valid @RequestBody UserRequest r,Authentication a){
        if(users.findByUsername(r.username()).isPresent())throw new IllegalArgumentException("El usuario ya existe");
        var saved=users.save(UserAccount.builder().username(r.username()).displayName(r.displayName())
          .passwordHash(encoder.encode(r.password())).role(Role.SECRETARIA).build());
        audit.log(a.getName(),"CREAR","USUARIO_SECRETARIA",saved.getId());return dto(saved);
    }

    @PatchMapping("/{id}/status") @Transactional
    public UserDto status(@PathVariable Long id,@RequestParam boolean active,Authentication a){
        var user=users.findById(id).orElseThrow(()->new IllegalArgumentException("Usuario no encontrado"));
        if(user.getRole()!=Role.SECRETARIA)throw new IllegalArgumentException("Solo se pueden gestionar usuarios de Secretaría aquí");
        user.setActivo(active);audit.log(a.getName(),active?"ACTIVAR":"DESACTIVAR","USUARIO_SECRETARIA",id);
        return dto(users.save(user));
    }
}
