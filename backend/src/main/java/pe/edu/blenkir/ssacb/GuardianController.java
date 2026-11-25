package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/guardians")
public class GuardianController {
    private final GuardianRepository guardians; private final GuardianStudentRepository links; private final StudentRepository students; private final UserAccountRepository users; private final PasswordEncoder encoder; private final GradeRepository grades; private final AttendanceRepository attendances; private final EnrollmentRepository enrollments; private final DniUniquenessService dniUniquenessService; private final AcademicRiskService academicRisk;
    public GuardianController(GuardianRepository guardians,GuardianStudentRepository links,StudentRepository students,UserAccountRepository users,PasswordEncoder encoder,GradeRepository grades,AttendanceRepository attendances,EnrollmentRepository enrollments,DniUniquenessService dniUniquenessService,AcademicRiskService academicRisk){this.guardians=guardians;this.links=links;this.students=students;this.users=users;this.encoder=encoder;this.grades=grades;this.attendances=attendances;this.enrollments=enrollments;this.dniUniquenessService=dniUniquenessService;this.academicRisk=academicRisk;}

    public record GuardianDto(Long id,String dni,String nombres,String apellidos,String telefono,String email,boolean activo){}
    public record SaveGuardian(@NotBlank @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos.") String dni,@NotBlank String nombres,@NotBlank String apellidos,String telefono,String email,@NotBlank String username,String password){}
    public record UpdateGuardian(@NotBlank @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos.") String dni,@NotBlank String nombres,@NotBlank String apellidos,String telefono,String email){}
    public record ChildDto(Long id,String codigo,String dni,String nombres,String apellidos,String grado,String seccion,String parentesco){}
    static GuardianDto dto(Guardian g){return new GuardianDto(g.getId(),g.getDni(),g.getNombres(),g.getApellidos(),g.getTelefono(),g.getEmail(),g.isActivo());}
    static ChildDto child(GuardianStudent l){var s=l.getStudent();return new ChildDto(s.getId(),s.getCodigo(),s.getDni(),s.getNombres(),s.getApellidos(),s.getGrado(),s.getSeccion(),l.getParentesco());}

    @GetMapping @Transactional(readOnly = true) @PreAuthorize("hasRole('ADMINISTRADOR')") public List<GuardianDto> all(){return guardians.findAll().stream().map(GuardianController::dto).toList();}
    @PostMapping @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public GuardianDto create(@Valid @RequestBody SaveGuardian x){
        String dni=dniUniquenessService.validateAndEnsureAvailable(x.dni());
        if(users.findByUsername(x.username()).isPresent())throw new IllegalArgumentException("El usuario ya existe");
        var user=users.save(Objects.requireNonNull(UserAccount.builder().username(x.username())
                .passwordHash(encoder.encode(AccountPasswordPolicy.requireInitial(x.password())))
                .displayName(x.nombres()+" "+x.apellidos()).role(Role.APODERADO).build()));
        var guardian=Objects.requireNonNull(Guardian.builder().dni(dni).nombres(x.nombres())
                .apellidos(x.apellidos()).telefono(x.telefono()).email(x.email()).user(user).build());
        return dto(guardians.save(guardian));
    }

    @PutMapping("/{id}") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public GuardianDto update(@PathVariable Long id,@Valid @RequestBody UpdateGuardian x){
        var guardian=guardians.findById(Objects.requireNonNull(id))
                .orElseThrow(()->new IllegalArgumentException("Apoderado no encontrado"));
        String dni=dniUniquenessService.validateAndEnsureAvailable(x.dni(),null,null,guardian.getId());
        guardian.setDni(dni);guardian.setNombres(x.nombres());guardian.setApellidos(x.apellidos());
        guardian.setTelefono(x.telefono());guardian.setEmail(x.email());
        return dto(guardians.save(guardian));
    }

    @PatchMapping("/{id}/status") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public GuardianDto status(@PathVariable Long id,@RequestParam boolean active){
        var guardian=guardians.findById(Objects.requireNonNull(id))
                .orElseThrow(()->new IllegalArgumentException("Apoderado no encontrado"));
        guardian.setActivo(active);
        return dto(guardians.save(guardian));
    }

    @PostMapping("/{guardianId}/link/{studentId}") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ChildDto link(@PathVariable Long guardianId,@PathVariable Long studentId,@RequestParam(defaultValue="Apoderado") String parentesco){
        var guardian=guardians.findById(Objects.requireNonNull(guardianId)).orElseThrow();
        var student=students.findById(Objects.requireNonNull(studentId)).orElseThrow();
        var existing=links.findByGuardian_IdAndStudent_IdAndAutorizadoTrue(guardianId,studentId);
        if(existing.isPresent()){
            existing.get().setParentesco(parentesco);
            return child(links.save(existing.get()));
        }
        var inactive=links.findByGuardian_IdAndStudent_Id(guardianId,studentId);
        if(inactive.isPresent()){
            var link=inactive.get();
            link.setAutorizado(true);link.setParentesco(parentesco);
            return child(links.save(link));
        }
        var link=Objects.requireNonNull(GuardianStudent.builder().guardian(guardian).student(student)
                .parentesco(parentesco).build());
        return child(links.save(link));
    }

    @GetMapping("/me/children") @Transactional(readOnly = true) @PreAuthorize("hasRole('APODERADO')")
    public List<ChildDto> myChildren(Authentication a){var g=guardians.findByUser_Username(a.getName()).orElseThrow();return links.findByGuardian_IdAndAutorizadoTrue(g.getId()).stream().map(GuardianController::child).toList();}

    @GetMapping("/me/children/dni/{dni}/summary") @Transactional(readOnly = true) @PreAuthorize("hasRole('APODERADO')")
    public Map<String,Object> childSummary(Authentication a,@PathVariable String dni){var g=guardians.findByUser_Username(a.getName()).orElseThrow();var link=links.findByGuardian_IdAndStudent_DniAndAutorizadoTrue(g.getId(),dni).orElseThrow(()->new IllegalArgumentException("El DNI no corresponde a un estudiante autorizado para este apoderado."));var s=link.getStudent();return studentSummary(s,link.getParentesco());}

    private Map<String,Object> studentSummary(Student s,String parentesco){
        var gs=grades.findByStudent_IdOrderByEvaluation_FechaDesc(s.getId()).stream().map(AcademicController::gradeDto).toList();
        var at=attendances.findByStudent_IdOrderByFechaDesc(s.getId()).stream().map(AcademicController::attendanceDto).toList();
        var cs=enrollments.findByStudent_IdAndActivoTrue(s.getId()).stream()
                .map(enrollment->Objects.requireNonNull(enrollment).getCourse())
                .map(CourseController::dto).toList();
        var out=new LinkedHashMap<String,Object>();out.put("student",new ChildDto(s.getId(),s.getCodigo(),s.getDni(),s.getNombres(),s.getApellidos(),s.getGrado(),s.getSeccion(),parentesco));out.put("courses",cs);out.put("grades",gs);out.put("attendance",at);out.put("alerts",academicRisk.listForStudent(s.getId()));return out;
    }
}
