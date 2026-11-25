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
    public CourseDto create(@Valid @RequestBody SaveCourse x){
        if(courses.findAll().stream().anyMatch(c->c.getCodigo().equalsIgnoreCase(x.codigo().trim())))
            throw new IllegalArgumentException("El código de curso ya existe");
        Long teacherId=x.docenteId();
        var teacher=teacherId==null?null:teachers.findById(Objects.requireNonNull(teacherId))
                .orElseThrow(()->new IllegalArgumentException("Docente no encontrado"));
        var course=Objects.requireNonNull(Course.builder().codigo(x.codigo()).nombre(x.nombre())
                .grado(x.grado()).seccion(x.seccion()).periodo(x.periodo()).teacher(teacher).build());
        return dto(courses.save(course));
    }

    @PutMapping("/{id}") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CourseDto update(@PathVariable Long id,@Valid @RequestBody SaveCourse x){
        var course=courses.findById(Objects.requireNonNull(id))
                .orElseThrow(()->new IllegalArgumentException("Curso no encontrado"));
        if(courses.findAll().stream().anyMatch(c->!Objects.equals(c.getId(),id)&&c.getCodigo().equalsIgnoreCase(x.codigo().trim())))
            throw new IllegalArgumentException("El código de curso ya existe");
        var teacher=x.docenteId()==null?null:teachers.findById(Objects.requireNonNull(x.docenteId()))
                .orElseThrow(()->new IllegalArgumentException("Docente no encontrado"));
        course.setCodigo(x.codigo().trim());course.setNombre(x.nombre());course.setGrado(x.grado());
        course.setSeccion(x.seccion());course.setPeriodo(x.periodo());course.setTeacher(teacher);
        return dto(courses.save(course));
    }

    @PatchMapping("/{id}/status") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CourseDto status(@PathVariable Long id,@RequestParam boolean active){
        var course=courses.findById(Objects.requireNonNull(id))
                .orElseThrow(()->new IllegalArgumentException("Curso no encontrado"));
        course.setActivo(active);
        return dto(courses.save(course));
    }

    @GetMapping("/{id}/students") @Transactional(readOnly = true) @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public List<StudentController.StudentDto> students(@PathVariable Long id,Authentication a){
        var course=courses.findById(Objects.requireNonNull(id))
                .orElseThrow(()->new IllegalArgumentException("Curso no encontrado"));
        if(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCENTE"))){
            var teacher=teachers.findByUser_Username(a.getName()).orElseThrow();
            if(course.getTeacher()==null||!Objects.equals(course.getTeacher().getId(),teacher.getId()))
                throw new IllegalArgumentException("El curso no está asignado al docente actual.");
        }
        return enrollments.findByCourse_IdAndActivoTrue(id).stream()
                .map(enrollment->Objects.requireNonNull(enrollment).getStudent())
                .map(StudentController::dto).toList();
    }

    @PostMapping("/{courseId}/enroll/{studentId}") @Transactional @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void enroll(@PathVariable Long courseId,@PathVariable Long studentId){
        var c=courses.findById(Objects.requireNonNull(courseId)).orElseThrow();
        var s=students.findById(Objects.requireNonNull(studentId)).orElseThrow();
        if(!c.isActivo()||!s.isActivo())throw new IllegalArgumentException("Estudiante o curso inactivo");
        if(!Objects.equals(s.getGrado(),c.getGrado())||!Objects.equals(s.getSeccion(),c.getSeccion()))
            throw new IllegalArgumentException("El grado y sección del estudiante deben coincidir con el curso");
        var e=enrollments.findByStudent_IdAndCourse_Id(studentId,courseId).orElse(Enrollment.builder().course(c).student(s).build());
        e.setActivo(true);
        enrollments.save(e);
    }
}
