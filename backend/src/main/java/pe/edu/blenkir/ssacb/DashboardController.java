package pe.edu.blenkir.ssacb;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final StudentRepository students;private final TeacherRepository teachers;private final CourseRepository courses;private final GradeRepository grades;private final TeacherRepository teacherRepo;private final EnrollmentRepository enrollments;private final GuardianRepository guardians;private final GuardianStudentRepository links;
    public DashboardController(StudentRepository students,TeacherRepository teachers,CourseRepository courses,GradeRepository grades,TeacherRepository teacherRepo,EnrollmentRepository enrollments,GuardianRepository guardians,GuardianStudentRepository links){this.students=students;this.teachers=teachers;this.courses=courses;this.grades=grades;this.teacherRepo=teacherRepo;this.enrollments=enrollments;this.guardians=guardians;this.links=links;}
    @GetMapping @Transactional(readOnly = true) public Map<String,Object> dashboard(Authentication a){var role=a.getAuthorities().stream().findFirst().map(x->x.getAuthority()).orElse("");var m=new LinkedHashMap<String,Object>();m.put("role",role.replace("ROLE_",""));if(role.equals("ROLE_ADMINISTRADOR")||role.equals("ROLE_SECRETARIA")){m.put("students",students.countByActivoTrue());m.put("teachers",teachers.countByActivoTrue());m.put("courses",courses.countByActivoTrue());m.put("grades",grades.count());}else if(role.equals("ROLE_DOCENTE")){var t=teacherRepo.findByUser_Username(a.getName()).orElseThrow();var cs=courses.findByTeacher_IdAndActivoTrue(t.getId());m.put("courses",cs.size());m.put("students",cs.stream().flatMap(c->enrollments.findByCourse_IdAndActivoTrue(c.getId()).stream()).map(e->e.getStudent().getId()).distinct().count());m.put("teacher",TeacherController.dto(t));}else if(role.equals("ROLE_ESTUDIANTE")){var s=students.findByUser_Username(a.getName()).orElseThrow();m.put("student",StudentController.dto(s));m.put("courses",enrollments.findByStudent_IdAndActivoTrue(s.getId()).size());m.put("grades",grades.findByStudent_IdOrderByEvaluation_FechaDesc(s.getId()).size());}else if(role.equals("ROLE_APODERADO")){var g=guardians.findByUser_Username(a.getName()).orElseThrow();m.put("guardian",GuardianController.dto(g));m.put("children",links.findByGuardian_IdAndAutorizadoTrue(g.getId()).size());}return m;}
}
