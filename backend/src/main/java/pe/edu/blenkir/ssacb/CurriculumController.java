package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
        var course=courses.findById(r.cursoId()).orElseThrow(()->new IllegalArgumentException("Curso no encontrado"));
        course.setTeacher(r.docenteId()==null?null:teachers.findById(r.docenteId()).orElseThrow(()->new IllegalArgumentException("Docente no encontrado")));
        audit.log(auth.getName(),"ASIGNAR_DOCENTE","CURSO",course.getId());return CourseController.dto(courses.save(course));
    }
}
