package pe.edu.blenkir.ssacb;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SECRETARIA')")
public class EnrollmentController {
    private final EnrollmentRepository enrollments;private final CourseRepository courses;
    private final StudentRepository students;private final AuditService audit;
    public EnrollmentController(EnrollmentRepository enrollments,CourseRepository courses,StudentRepository students,AuditService audit){this.enrollments=enrollments;this.courses=courses;this.students=students;this.audit=audit;}
    public record EnrollmentDto(Long id,Long estudianteId,String estudiante,Long cursoId,String curso,String grado,String seccion,boolean activo){}
    static EnrollmentDto dto(Enrollment e){return new EnrollmentDto(e.getId(),e.getStudent().getId(),e.getStudent().getNombres()+" "+e.getStudent().getApellidos(),e.getCourse().getId(),e.getCourse().getNombre(),e.getCourse().getGrado(),e.getCourse().getSeccion(),e.isActivo());}
    @GetMapping @Transactional(readOnly=true)
    public List<EnrollmentDto> list(){return enrollments.findAll().stream().map(EnrollmentController::dto).toList();}
    @PostMapping @Transactional
    public EnrollmentDto enroll(@RequestParam Long studentId,@RequestParam Long courseId,Authentication auth){
        var student=students.findById(studentId).orElseThrow(()->new IllegalArgumentException("Estudiante no encontrado"));
        var course=courses.findById(courseId).orElseThrow(()->new IllegalArgumentException("Curso no encontrado"));
        if(!student.isActivo()||!course.isActivo())throw new IllegalArgumentException("Estudiante o curso inactivo");
        if(!java.util.Objects.equals(student.getGrado(),course.getGrado()) || !java.util.Objects.equals(student.getSeccion(),course.getSeccion()))
            throw new IllegalArgumentException("El grado y sección del estudiante deben coincidir con el curso");
        var e=enrollments.findByStudent_IdAndCourse_Id(studentId,courseId).orElse(Enrollment.builder().student(student).course(course).build());
        e.setActivo(true);var saved=enrollments.save(e);audit.log(auth.getName(),"MATRICULAR","MATRICULA",saved.getId());return dto(saved);
    }
    @PatchMapping("/{id}/status") @Transactional
    public EnrollmentDto status(@PathVariable Long id,@RequestParam boolean active,Authentication auth){
        var e=enrollments.findById(id).orElseThrow(()->new IllegalArgumentException("Matrícula no encontrada"));
        e.setActivo(active);audit.log(auth.getName(),active?"REACTIVAR":"DESACTIVAR","MATRICULA",id);
        return dto(enrollments.save(e));
    }
}
