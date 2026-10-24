package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/competencies")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
public class CompetencyController {
    private final CompetencyRepository competencies;
    private final CompetencyResultRepository results;
    private final CourseRepository courses;
    private final TeacherRepository teachers;
    private final StudentRepository students;
    private final EnrollmentRepository enrollments;
    private final AuditService audit;
    public CompetencyController(CompetencyRepository competencies,CompetencyResultRepository results,CourseRepository courses,
     TeacherRepository teachers,StudentRepository students,EnrollmentRepository enrollments,AuditService audit){
        this.competencies=competencies;this.results=results;this.courses=courses;this.teachers=teachers;this.students=students;this.enrollments=enrollments;this.audit=audit;
    }
    public record CompetencyInput(@NotNull Long cursoId,@NotBlank String nombre,String descripcion){}
    public record ResultInput(@NotNull Long competenciaId,@NotNull Long estudianteId,@NotBlank String periodo,@NotBlank String nivel,String conclusion){}
    public record CompetencyDto(Long id,Long cursoId,String curso,String nombre,String descripcion){}
    public record ResultDto(Long id,Long competenciaId,String competencia,Long estudianteId,String estudiante,String periodo,String nivel,String conclusion){}
    static CompetencyDto dto(Competency c){return new CompetencyDto(c.getId(),c.getCourse().getId(),c.getCourse().getNombre(),c.getNombre(),c.getDescripcion());}
    static ResultDto dto(CompetencyResult r){return new ResultDto(r.getId(),r.getCompetency().getId(),r.getCompetency().getNombre(),r.getStudent().getId(),r.getStudent().getNombres()+" "+r.getStudent().getApellidos(),r.getPeriodo(),r.getNivel(),r.getConclusion());}

    private void allowed(Course c,Authentication a){
        if(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCENTE"))){
            var t=teachers.findByUser_Username(a.getName()).orElseThrow();
            if(c.getTeacher()==null||!Objects.equals(c.getTeacher().getId(),t.getId()))throw new org.springframework.security.access.AccessDeniedException("Curso no asignado");
        }
    }
    @GetMapping @Transactional(readOnly=true)
    public List<CompetencyDto> list(Authentication a){
        if(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCENTE"))){
            var t=teachers.findByUser_Username(a.getName()).orElseThrow();
            var ids=courses.findByTeacher_IdAndActivoTrue(t.getId()).stream().map(Course::getId).toList();
            return competencies.findAll().stream().filter(c->ids.contains(c.getCourse().getId())).map(CompetencyController::dto).toList();
        }
        return competencies.findAll().stream().map(CompetencyController::dto).toList();
    }
    @PostMapping @Transactional
    public CompetencyDto create(@Valid @RequestBody CompetencyInput r,Authentication a){
        var course=courses.findById(r.cursoId()).orElseThrow(()->new IllegalArgumentException("Curso no encontrado"));allowed(course,a);
        var c=competencies.save(Competency.builder().course(course).nombre(r.nombre()).descripcion(r.descripcion()).build());
        audit.log(a.getName(),"CREAR","COMPETENCIA",c.getId());return dto(c);
    }
    @GetMapping("/results") @Transactional(readOnly=true)
    public List<ResultDto> results(Authentication a){
        if(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCENTE"))){
            var t=teachers.findByUser_Username(a.getName()).orElseThrow();
            var ids=courses.findByTeacher_IdAndActivoTrue(t.getId()).stream().map(Course::getId).toList();
            return results.findAll().stream().filter(r->ids.contains(r.getCompetency().getCourse().getId())).map(CompetencyController::dto).toList();
        }
        return results.findAll().stream().map(CompetencyController::dto).toList();
    }
    @PostMapping("/results") @Transactional
    public ResultDto save(@Valid @RequestBody ResultInput r,Authentication a){
        if(!Set.of("AD","A","B","C").contains(r.nivel()))throw new IllegalArgumentException("El nivel debe ser AD, A, B o C");
        var c=competencies.findById(r.competenciaId()).orElseThrow(()->new IllegalArgumentException("Competencia no encontrada"));allowed(c.getCourse(),a);
        var s=students.findById(r.estudianteId()).orElseThrow(()->new IllegalArgumentException("Estudiante no encontrado"));
        if(enrollments.findByStudent_IdAndCourse_Id(s.getId(),c.getCourse().getId()).filter(Enrollment::isActivo).isEmpty())
            throw new IllegalArgumentException("Estudiante no matriculado en el curso");
        var result=results.findByCompetency_IdAndStudent_IdAndPeriodo(c.getId(),s.getId(),r.periodo()).orElse(CompetencyResult.builder().competency(c).student(s).periodo(r.periodo()).build());
        result.setNivel(r.nivel());result.setConclusion(r.conclusion());var saved=results.save(result);
        audit.log(a.getName(),"GUARDAR_LOGRO","COMPETENCIA_RESULTADO",saved.getId());return dto(saved);
    }
}
