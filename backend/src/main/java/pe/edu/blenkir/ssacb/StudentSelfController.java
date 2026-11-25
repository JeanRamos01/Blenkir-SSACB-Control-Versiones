package pe.edu.blenkir.ssacb;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/student/me")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class StudentSelfController {
    private final StudentRepository students; private final EnrollmentRepository enrollments; private final GradeRepository grades; private final AttendanceRepository attendances; private final AcademicRiskService academicRisk;
    public StudentSelfController(StudentRepository students,EnrollmentRepository enrollments,GradeRepository grades,AttendanceRepository attendances,AcademicRiskService academicRisk){this.students=students;this.enrollments=enrollments;this.grades=grades;this.attendances=attendances;this.academicRisk=academicRisk;}

    @GetMapping("/summary") @Transactional(readOnly = true)
    public Map<String,Object> summary(Authentication a){
        var student=students.findByUser_Username(a.getName()).orElseThrow();
        var out=new LinkedHashMap<String,Object>();
        out.put("student",StudentController.dto(student));
        out.put("courses",enrollments.findByStudent_IdAndActivoTrue(student.getId()).stream()
                .map(enrollment->Objects.requireNonNull(enrollment).getCourse())
                .map(CourseController::dto).toList());
        out.put("grades",grades.findByStudent_IdOrderByEvaluation_FechaDesc(student.getId()).stream()
                .map(AcademicController::gradeDto).toList());
        out.put("attendance",attendances.findByStudent_IdOrderByFechaDesc(student.getId()).stream()
                .map(AcademicController::attendanceDto).toList());
        out.put("alerts",academicRisk.listForStudent(student.getId()));
        return out;
    }
}
