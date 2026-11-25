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
        if(teachers.findAll().stream().anyMatch(t->t.getCodigo().equalsIgnoreCase(x.codigo().trim())))
            throw new IllegalArgumentException("El código de docente ya existe");
        UserAccount user=null;
        if(x.username()!=null&&!x.username().isBlank()){
            if(users.findByUsername(x.username()).isPresent())throw new IllegalArgumentException("El usuario ya existe");
            var account=Objects.requireNonNull(UserAccount.builder().username(x.username())
                    .passwordHash(encoder.encode(AccountPasswordPolicy.requireInitial(x.password())))
                    .displayName(x.nombres()+" "+x.apellidos()).role(Role.DOCENTE).build());
            user=users.save(account);
        }
        var teacher=Objects.requireNonNull(Teacher.builder().codigo(x.codigo()).dni(dni).nombres(x.nombres())
                .apellidos(x.apellidos()).email(x.email()).especialidad(x.especialidad()).user(user).build());
        return dto(teachers.save(teacher));
    }

    @PutMapping("/{id}") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public TeacherDto update(@PathVariable Long id,@Valid @RequestBody SaveTeacher x){
        var teacher=teachers.findById(Objects.requireNonNull(id))
                .orElseThrow(()->new IllegalArgumentException("Docente no encontrado"));
        String dni=dniUniquenessService.validateAndEnsureAvailable(x.dni(),null,teacher.getId(),null);
        if(teachers.findAll().stream().anyMatch(t->!Objects.equals(t.getId(),id)&&t.getCodigo().equalsIgnoreCase(x.codigo().trim())))
            throw new IllegalArgumentException("El código de docente ya existe");
        teacher.setCodigo(x.codigo().trim());teacher.setDni(dni);teacher.setNombres(x.nombres());
        teacher.setApellidos(x.apellidos());teacher.setEmail(x.email());teacher.setEspecialidad(x.especialidad());
        return dto(teachers.save(teacher));
    }

    @PatchMapping("/{id}/status") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public TeacherDto status(@PathVariable Long id,@RequestParam boolean active){
        var teacher=teachers.findById(Objects.requireNonNull(id))
                .orElseThrow(()->new IllegalArgumentException("Docente no encontrado"));
        teacher.setActivo(active);
        return dto(teachers.save(teacher));
    }
}
