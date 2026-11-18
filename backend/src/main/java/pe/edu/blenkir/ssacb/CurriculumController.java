package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/curriculum")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class CurriculumController {
    private final CourseRepository courses;private final TeacherRepository teachers;private final AuditService audit;
    public CurriculumController(CourseRepository courses,TeacherRepository teachers,AuditService audit){this.courses=courses;this.teachers=teachers;this.audit=audit;}
    public record Assignment(@NotNull Long cursoId,Long docenteId){}
    @GetMapping @Transactional(readOnly=true)
    public List<CourseController.CourseDto> list(){return courses.findAll().stream().map(CourseController::dto).toList();}
    @PutMapping("/assignment") @Transactional
    public CourseController.CourseDto assign(@Valid @RequestBody Assignment r,Authentication auth){
        var course=courses.findById(Objects.requireNonNull(r.cursoId()))
                .orElseThrow(()->new IllegalArgumentException("Curso no encontrado"));
        Long teacherId=r.docenteId();
        var teacher=teacherId==null?null:teachers.findById(Objects.requireNonNull(teacherId))
                .orElseThrow(()->new IllegalArgumentException("Docente no encontrado"));
        course.setTeacher(teacher);
        audit.log(auth.getName(),"ASIGNAR_DOCENTE","CURSO",course.getId());return CourseController.dto(courses.save(course));
    }
}
