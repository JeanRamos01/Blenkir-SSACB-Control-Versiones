package pe.edu.blenkir.ssacb;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentRepository students;
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final DniUniquenessService dniUniquenessService;
    private final TeacherRepository teachers;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    public StudentController(StudentRepository students, UserAccountRepository users, PasswordEncoder encoder, DniUniquenessService dniUniquenessService,
        TeacherRepository teachers, CourseRepository courses, EnrollmentRepository enrollments) {
        this.students = students;
        this.users = users;
        this.encoder = encoder;
        this.dniUniquenessService = dniUniquenessService;
        this.teachers=teachers;this.courses=courses;this.enrollments=enrollments;
    }

    public record StudentDto(Long id,String codigo,String dni,String nombres,String apellidos,LocalDate fechaNacimiento,String grado,String seccion,String email,String direccion,String telefono,boolean activo){}
    public record SaveStudent(@NotBlank String codigo,@NotBlank @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos.") String dni,@NotBlank String nombres,@NotBlank String apellidos,LocalDate fechaNacimiento,String grado,String seccion,String email,String direccion,String telefono,String username,String password){}
    static StudentDto dto(Student s){return new StudentDto(s.getId(),s.getCodigo(),s.getDni(),s.getNombres(),s.getApellidos(),s.getFechaNacimiento(),s.getGrado(),s.getSeccion(),s.getEmail(),s.getDireccion(),s.getTelefono(),s.isActivo());}

    @GetMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE','SECRETARIA')")
    public List<StudentDto> all(Authentication auth){
        if(auth.getAuthorities().stream().anyMatch(role->role.getAuthority().equals("ROLE_DOCENTE"))){
            var teacher=teachers.findByUser_Username(auth.getName()).orElseThrow();
            var ids=courses.findByTeacher_IdAndActivoTrue(teacher.getId()).stream()
                .flatMap(course->enrollments.findByCourse_IdAndActivoTrue(course.getId()).stream())
                .map(enrollment->enrollment.getStudent().getId()).collect(java.util.stream.Collectors.toSet());
            return students.findAllById(ids).stream().map(StudentController::dto).toList();
        }
        return students.findAll().stream().map(StudentController::dto).toList();
    }

    @PostMapping @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public StudentDto create(@Valid @RequestBody SaveStudent x){
        String dni = dniUniquenessService.validateAndEnsureAvailable(x.dni());
        UserAccount user=null;
        if(x.username()!=null && !x.username().isBlank()){
            if(users.findByUsername(x.username()).isPresent()) throw new IllegalArgumentException("El usuario ya existe");
            user=users.save(UserAccount.builder().username(x.username()).passwordHash(encoder.encode(AccountPasswordPolicy.requireInitial(x.password()))).displayName(x.nombres()+" "+x.apellidos()).role(Role.ESTUDIANTE).build());
        }
        var s=Student.builder().codigo(x.codigo()).dni(dni).nombres(x.nombres()).apellidos(x.apellidos()).fechaNacimiento(x.fechaNacimiento()).grado(x.grado()).seccion(x.seccion()).email(x.email()).direccion(x.direccion()).telefono(x.telefono()).user(user).build();
        return dto(students.save(s));
    }

    @PutMapping("/{id}") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public StudentDto update(@PathVariable Long id,@Valid @RequestBody SaveStudent x){
        var s=students.findById(id).orElseThrow(()->new IllegalArgumentException("Estudiante no encontrado"));
        String dni = dniUniquenessService.validateAndEnsureAvailable(x.dni(), s.getId(), null, null);
        s.setCodigo(x.codigo());s.setDni(dni);s.setNombres(x.nombres());s.setApellidos(x.apellidos());s.setFechaNacimiento(x.fechaNacimiento());s.setGrado(x.grado());s.setSeccion(x.seccion());s.setEmail(x.email());s.setDireccion(x.direccion());s.setTelefono(x.telefono());
        return dto(students.save(s));
    }

    @PatchMapping("/{id}/status") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public StudentDto status(@PathVariable Long id,@RequestParam boolean active){var s=students.findById(id).orElseThrow();s.setActivo(active);return dto(students.save(s));}
}
