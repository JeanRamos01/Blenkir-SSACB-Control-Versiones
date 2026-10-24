package pe.edu.blenkir.ssacb;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    private final TeacherRepository teachers;
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final DniUniquenessService dniUniquenessService;

    public TeacherController(TeacherRepository teachers, UserAccountRepository users, PasswordEncoder encoder, DniUniquenessService dniUniquenessService) {
        this.teachers = teachers;
        this.users = users;
        this.encoder = encoder;
        this.dniUniquenessService = dniUniquenessService;
    }
    public record TeacherDto(Long id,String codigo,String dni,String nombres,String apellidos,String email,String especialidad,boolean activo){}
    public record SaveTeacher(@NotBlank String codigo,@NotBlank @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos.") String dni,@NotBlank String nombres,@NotBlank String apellidos,String email,String especialidad,String username,String password){}
    static TeacherDto dto(Teacher t){return new TeacherDto(t.getId(),t.getCodigo(),t.getDni(),t.getNombres(),t.getApellidos(),t.getEmail(),t.getEspecialidad(),t.isActivo());}

    @GetMapping @PreAuthorize("hasRole('ADMINISTRADOR')") public List<TeacherDto> all(){return teachers.findAll().stream().map(TeacherController::dto).toList();}
    @PostMapping @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')") public TeacherDto create(@Valid @RequestBody SaveTeacher x){
        String dni = dniUniquenessService.validateAndEnsureAvailable(x.dni());
        UserAccount u=null; if(x.username()!=null&&!x.username().isBlank()){ if(users.findByUsername(x.username()).isPresent())throw new IllegalArgumentException("El usuario ya existe"); u=users.save(UserAccount.builder().username(x.username()).passwordHash(encoder.encode(AccountPasswordPolicy.requireInitial(x.password()))).displayName(x.nombres()+" "+x.apellidos()).role(Role.DOCENTE).build()); }
        return dto(teachers.save(Teacher.builder().codigo(x.codigo()).dni(dni).nombres(x.nombres()).apellidos(x.apellidos()).email(x.email()).especialidad(x.especialidad()).user(u).build()));
    }
}
