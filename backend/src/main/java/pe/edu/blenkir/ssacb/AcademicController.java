package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/academic")
public class AcademicController {
    private final EvaluationRepository evaluations; private final GradeRepository grades; private final AttendanceRepository attendances; private final CourseRepository courses; private final StudentRepository students; private final TeacherRepository teachers; private final EnrollmentRepository enrollments;
    public AcademicController(EvaluationRepository evaluations,GradeRepository grades,AttendanceRepository attendances,CourseRepository courses,StudentRepository students,TeacherRepository teachers,EnrollmentRepository enrollments){this.evaluations=evaluations;this.grades=grades;this.attendances=attendances;this.courses=courses;this.students=students;this.teachers=teachers;this.enrollments=enrollments;}

    public record EvaluationDto(Long id,Long cursoId,String curso,String nombre,String tipo,LocalDate fecha,Integer peso){}
    public record SaveEvaluation(@NotNull Long cursoId,@NotBlank String nombre,String tipo,@NotNull LocalDate fecha,Integer peso){}
    public record GradeDto(Long id,Long evaluacionId,String evaluacion,Long cursoId,String curso,Long estudianteId,String estudiante,Double nota,String observacion,LocalDate fecha){}
    public record SaveGrade(@NotNull Long evaluacionId,@NotNull Long estudianteId,@NotNull @DecimalMin("0") @DecimalMax("20") Double nota,String observacion){}
    public record AttendanceDto(Long id,Long cursoId,String curso,Long estudianteId,String estudiante,LocalDate fecha,String estado,String observacion){}
    public record SaveAttendance(@NotNull Long cursoId,@NotNull Long estudianteId,@NotNull LocalDate fecha,@NotBlank String estado,String observacion){}

    static EvaluationDto evaluationDto(Evaluation e){return new EvaluationDto(e.getId(),e.getCourse().getId(),e.getCourse().getNombre(),e.getNombre(),e.getTipo(),e.getFecha(),e.getPeso());}
    static GradeDto gradeDto(Grade g){var e=g.getEvaluation();var s=g.getStudent();return new GradeDto(g.getId(),e.getId(),e.getNombre(),e.getCourse().getId(),e.getCourse().getNombre(),s.getId(),s.getNombres()+" "+s.getApellidos(),g.getNota(),g.getObservacion(),e.getFecha());}
    static AttendanceDto attendanceDto(Attendance x){return new AttendanceDto(x.getId(),x.getCourse().getId(),x.getCourse().getNombre(),x.getStudent().getId(),x.getStudent().getNombres()+" "+x.getStudent().getApellidos(),x.getFecha(),x.getEstado().name(),x.getObservacion());}

    @GetMapping("/evaluations") @Transactional(readOnly = true) @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public List<EvaluationDto> evaluations(@RequestParam(required=false) Long courseId,Authentication a){
        if(courseId!=null){var c=courses.findById(courseId).orElseThrow();ensureCourseAllowed(c,a);return evaluations.findByCourse_IdOrderByFechaDesc(courseId).stream().map(AcademicController::evaluationDto).toList();}
        if(isRole(a,"DOCENTE")){var t=teachers.findByUser_Username(a.getName()).orElseThrow();return evaluations.findByCourse_Teacher_IdOrderByFechaDesc(t.getId()).stream().map(AcademicController::evaluationDto).toList();}
        return evaluations.findAll().stream().map(AcademicController::evaluationDto).toList();
    }

    @PostMapping("/evaluations") @Transactional @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public EvaluationDto createEvaluation(@Valid @RequestBody SaveEvaluation x,Authentication a){var c=courses.findById(x.cursoId()).orElseThrow();ensureCourseAllowed(c,a);return evaluationDto(evaluations.save(Evaluation.builder().course(c).nombre(x.nombre()).tipo(x.tipo()).fecha(x.fecha()).peso(x.peso()==null?100:x.peso()).build()));}

    @GetMapping("/grades") @Transactional(readOnly = true) @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public List<GradeDto> grades(@RequestParam(required=false) Long courseId,Authentication a){
        if(courseId!=null){var c=courses.findById(courseId).orElseThrow();ensureCourseAllowed(c,a);return grades.findByEvaluation_Course_IdOrderByEvaluation_FechaDesc(courseId).stream().map(AcademicController::gradeDto).toList();}
        if(isRole(a,"DOCENTE")){var t=teachers.findByUser_Username(a.getName()).orElseThrow();return courses.findByTeacher_IdAndActivoTrue(t.getId()).stream().flatMap(c->grades.findByEvaluation_Course_IdOrderByEvaluation_FechaDesc(c.getId()).stream()).map(AcademicController::gradeDto).toList();}
        return grades.findAll().stream().map(AcademicController::gradeDto).toList();
    }

    @PostMapping("/grades") @Transactional @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public GradeDto saveGrade(@Valid @RequestBody SaveGrade x,Authentication a){var e=evaluations.findById(x.evaluacionId()).orElseThrow();ensureCourseAllowed(e.getCourse(),a);var s=students.findById(x.estudianteId()).orElseThrow();if(enrollments.findByStudent_IdAndCourse_Id(s.getId(),e.getCourse().getId()).filter(Enrollment::isActivo).isEmpty()) throw new IllegalArgumentException("El estudiante no está matriculado en el curso.");var g=grades.findByEvaluation_IdAndStudent_Id(e.getId(),s.getId()).orElse(Grade.builder().evaluation(e).student(s).build());g.setNota(x.nota());g.setObservacion(x.observacion());return gradeDto(grades.save(g));}

    @GetMapping("/attendance") @Transactional(readOnly = true) @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public List<AttendanceDto> attendance(@RequestParam(required=false) Long courseId,Authentication a){
        if(courseId!=null){var c=courses.findById(courseId).orElseThrow();ensureCourseAllowed(c,a);return attendances.findByCourse_IdOrderByFechaDesc(courseId).stream().map(AcademicController::attendanceDto).toList();}
        if(isRole(a,"DOCENTE")){var t=teachers.findByUser_Username(a.getName()).orElseThrow();return courses.findByTeacher_IdAndActivoTrue(t.getId()).stream().flatMap(c->attendances.findByCourse_IdOrderByFechaDesc(c.getId()).stream()).map(AcademicController::attendanceDto).toList();}
        return attendances.findAll().stream().map(AcademicController::attendanceDto).toList();
    }

    @PostMapping("/attendance") @Transactional @PreAuthorize("hasAnyRole('ADMINISTRADOR','DOCENTE')")
    public AttendanceDto saveAttendance(@Valid @RequestBody SaveAttendance x,Authentication a){var c=courses.findById(x.cursoId()).orElseThrow();ensureCourseAllowed(c,a);var s=students.findById(x.estudianteId()).orElseThrow();if(enrollments.findByStudent_IdAndCourse_Id(s.getId(),c.getId()).filter(Enrollment::isActivo).isEmpty()) throw new IllegalArgumentException("El estudiante no está matriculado en el curso.");Attendance.Estado estado;try{estado=Attendance.Estado.valueOf(x.estado().toUpperCase());}catch(Exception ex){throw new IllegalArgumentException("Estado de asistencia inválido");}var item=attendances.findByCourse_IdAndStudent_IdAndFecha(c.getId(),s.getId(),x.fecha()).orElse(Attendance.builder().course(c).student(s).fecha(x.fecha()).build());item.setEstado(estado);item.setObservacion(x.observacion());return attendanceDto(attendances.save(item));}

    private void ensureCourseAllowed(Course c,Authentication a){if(isRole(a,"DOCENTE")){var t=teachers.findByUser_Username(a.getName()).orElseThrow();if(c.getTeacher()==null||!Objects.equals(c.getTeacher().getId(),t.getId()))throw new IllegalArgumentException("El curso no está asignado al docente actual.");}}
    private boolean isRole(Authentication a,String role){return a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_"+role));}
}
