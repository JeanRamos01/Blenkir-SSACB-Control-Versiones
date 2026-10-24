package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/schedules")
public class SchoolScheduleController {
    private final SchoolScheduleRepository schedules;
    private final CourseRepository courses;
    private final TeacherRepository teachers;
    private final StudentRepository students;
    private final EnrollmentRepository enrollments;
    private final AuditService audit;
    public SchoolScheduleController(SchoolScheduleRepository schedules,CourseRepository courses,TeacherRepository teachers,
        StudentRepository students,EnrollmentRepository enrollments,AuditService audit){
        this.schedules=schedules;this.courses=courses;this.teachers=teachers;this.students=students;this.enrollments=enrollments;this.audit=audit;
    }
    public record ScheduleRequest(@NotNull Long cursoId,@NotNull @Min(1) @Max(7) Integer diaSemana,
        @NotNull LocalTime horaInicio,@NotNull LocalTime horaFin,String aula){}
    public record ScheduleDto(Long id,Long cursoId,String curso,String grado,String seccion,Integer diaSemana,LocalTime horaInicio,LocalTime horaFin,String aula){}
    static ScheduleDto dto(SchoolSchedule x){var c=x.getCourse();return new ScheduleDto(x.getId(),c.getId(),c.getNombre(),c.getGrado(),c.getSeccion(),x.getDiaSemana(),x.getHoraInicio(),x.getHoraFin(),x.getAula());}

    @GetMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','SECRETARIA','DOCENTE','ESTUDIANTE')") @Transactional(readOnly=true)
    public List<ScheduleDto> list(Authentication auth){
        String role=auth.getAuthorities().iterator().next().getAuthority();
        if(role.equals("ROLE_DOCENTE")){
            var teacher=teachers.findByUser_Username(auth.getName()).orElseThrow();
            var ids=courses.findByTeacher_IdAndActivoTrue(teacher.getId()).stream().map(Course::getId).toList();
            return ids.isEmpty()?List.of():schedules.findByCourse_IdInOrderByDiaSemanaAscHoraInicioAsc(ids).stream().map(SchoolScheduleController::dto).toList();
        }
        if(role.equals("ROLE_ESTUDIANTE")){
            var student=students.findByUser_Username(auth.getName()).orElseThrow();
            var ids=enrollments.findByStudent_IdAndActivoTrue(student.getId()).stream().map(e->e.getCourse().getId()).toList();
            return ids.isEmpty()?List.of():schedules.findByCourse_IdInOrderByDiaSemanaAscHoraInicioAsc(ids).stream().map(SchoolScheduleController::dto).toList();
        }
        return schedules.findAllByOrderByDiaSemanaAscHoraInicioAsc().stream().map(SchoolScheduleController::dto).toList();
    }

    @PostMapping @PreAuthorize("hasRole('ADMINISTRADOR')") @Transactional
    public ScheduleDto create(@Valid @RequestBody ScheduleRequest r,Authentication auth){
        if(!r.horaInicio().isBefore(r.horaFin()))throw new IllegalArgumentException("Hora final debe ser posterior a la inicial");
        var course=courses.findById(r.cursoId()).orElseThrow(()->new IllegalArgumentException("Curso no encontrado"));
        for(var other:schedules.findAllByOrderByDiaSemanaAscHoraInicioAsc()){
            if(!other.getDiaSemana().equals(r.diaSemana()) ||
               !(r.horaInicio().isBefore(other.getHoraFin()) && other.getHoraInicio().isBefore(r.horaFin())))continue;
            var existing=other.getCourse();
            boolean sameSection=Objects.equals(existing.getGrado(),course.getGrado())&&Objects.equals(existing.getSeccion(),course.getSeccion());
            boolean sameTeacher=course.getTeacher()!=null && existing.getTeacher()!=null && Objects.equals(course.getTeacher().getId(),existing.getTeacher().getId());
            boolean sameRoom=r.aula()!=null && !r.aula().isBlank() && r.aula().equalsIgnoreCase(other.getAula());
            if(sameSection || sameTeacher || sameRoom)throw new IllegalArgumentException("Cruce de horario en sección, docente o aula");
        }
        var item=schedules.save(SchoolSchedule.builder().course(course).diaSemana(r.diaSemana())
         .horaInicio(r.horaInicio()).horaFin(r.horaFin()).aula(r.aula()).build());
        audit.log(auth.getName(),"CREAR","HORARIO",item.getId());return dto(item);
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')") @Transactional
    public void delete(@PathVariable Long id,Authentication auth){
        if(!schedules.existsById(id))throw new IllegalArgumentException("Horario no encontrado");
        schedules.deleteById(id);audit.log(auth.getName(),"ELIMINAR","HORARIO",id);
    }
}
