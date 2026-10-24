package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseRepository courses; private final TeacherRepository teachers; private final EnrollmentRepository enrollments; private final StudentRepository students;
    public CourseController(CourseRepository courses,TeacherRepository teachers,EnrollmentRepository enrollments,StudentRepository students){this.courses=courses;this.teachers=teachers;this.enrollments=enrollments;this.students=students;}
    public record CourseDto(Long id,String codigo,String nombre,String grado,String seccion,String periodo,Long docenteId,String docente,boolean activo){}
    public record SaveCourse(@NotBlank String codigo,@NotBlank String nombre,String grado,String seccion,String periodo,Long docenteId){}
    static CourseDto dto(Course c){return new CourseDto(c.getId(),c.getCodigo(),c.getNombre(),c.getGrado(),c.getSeccion(),c.getPeriodo(),c.getTeacher()==null?null:c.getTeacher().getId(),c.getTeacher()==null?null:c.getTeacher().getNombres()+" "+c.getTeacher().getApellidos(),c.isActivo());}

    @GetMapping @Transactional(readOnly = true) @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE','SECRETARIA')")
    public List<CourseDto> all(Authentication a){
        boolean teacher=a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCENTE"));
        if(teacher){var t=teachers.findByUser_Username(a.getName()).orElseThrow();return courses.findByTeacher_IdAndActivoTrue(t.getId()).stream().map(CourseController::dto).toList();}
        return courses.findAll().stream().map(CourseController::dto).toList();
    }

    @PostMapping @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CourseDto create(@Valid @RequestBody SaveCourse x){var t=x.docenteId()==null?null:teachers.findById(x.docenteId()).orElseThrow(()->new IllegalArgumentException("Docente no encontrado"));return dto(courses.save(Course.builder().codigo(x.codigo()).nombre(x.nombre()).grado(x.grado()).seccion(x.seccion()).periodo(x.periodo()).teacher(t).build()));}

    @GetMapping("/{id}/students") @Transactional(readOnly = true) @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public List<StudentController.StudentDto> students(@PathVariable Long id,Authentication a){
        var course=courses.findById(id).orElseThrow(()->new IllegalArgumentException("Curso no encontrado"));
        if(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCENTE"))){
            var teacher=teachers.findByUser_Username(a.getName()).orElseThrow();
            if(course.getTeacher()==null||!Objects.equals(course.getTeacher().getId(),teacher.getId()))
                throw new IllegalArgumentException("El curso no está asignado al docente actual.");
        }
        return enrollments.findByCourse_IdAndActivoTrue(id).stream().map(Enrollment::getStudent).map(StudentController::dto).toList();
    }

    @PostMapping("/{courseId}/enroll/{studentId}") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void enroll(@PathVariable Long courseId,@PathVariable Long studentId){var c=courses.findById(courseId).orElseThrow();var s=students.findById(studentId).orElseThrow();var e=enrollments.findByStudent_IdAndCourse_Id(studentId,courseId).orElse(Enrollment.builder().course(c).student(s).build());e.setActivo(true);enrollments.save(e);}
}
