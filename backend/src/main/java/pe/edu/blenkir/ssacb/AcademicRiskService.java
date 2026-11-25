package pe.edu.blenkir.ssacb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AcademicRiskService {
    private final StudentRepository students;
    private final EnrollmentRepository enrollments;
    private final GradeRepository grades;
    private final AcademicRiskAlertRepository alerts;
    private final TeacherRepository teachers;
    private final double threshold;

    public AcademicRiskService(StudentRepository students, EnrollmentRepository enrollments,
            GradeRepository grades, AcademicRiskAlertRepository alerts, TeacherRepository teachers,
            @Value("${app.academic-risk.threshold:11.0}") double threshold) {
        this.students = students;
        this.enrollments = enrollments;
        this.grades = grades;
        this.alerts = alerts;
        this.teachers = teachers;
        if (!Double.isFinite(threshold) || threshold < 0 || threshold > 20)
            throw new IllegalArgumentException("ACADEMIC_RISK_THRESHOLD debe estar entre 0 y 20.");
        this.threshold = threshold;
    }

    public record AlertDto(Long id, Long estudianteId, String estudiante, Long cursoId, String curso,
            String periodo, String motivo, Double promedio, Double umbral, String estado,
            java.time.LocalDateTime fechaGeneracion) {}

    static AlertDto dto(AcademicRiskAlert alert) {
        Student student = alert.getStudent();
        Course course = alert.getCourse();
        return new AlertDto(alert.getId(), student.getId(), student.getNombres() + " " + student.getApellidos(),
                course.getId(), course.getNombre(), alert.getPeriodo(), alert.getMotivo(),
                alert.getPromedio(), alert.getUmbral(), alert.getEstado(), alert.getFechaGeneracion());
    }

    @Transactional
    public void evaluateStudent(Long studentId) {
        Student student = students.findById(Objects.requireNonNull(studentId))
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        if (!student.isActivo()) return;

        Set<Long> activeCourseIds = enrollments.findByStudent_IdAndActivoTrue(studentId).stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toSet());
        if (activeCourseIds.isEmpty()) return;

        Map<Course, List<Grade>> gradesByCourse = grades.findByStudent_IdOrderByEvaluation_FechaDesc(studentId).stream()
                .filter(grade -> activeCourseIds.contains(grade.getEvaluation().getCourse().getId()))
                .collect(Collectors.groupingBy(grade -> grade.getEvaluation().getCourse()));

        for (Map.Entry<Course, List<Grade>> entry : gradesByCourse.entrySet()) {
            Course course = entry.getKey();
            if (course.getPeriodo() == null || course.getPeriodo().isBlank()) continue;
            double average = entry.getValue().stream().mapToDouble(Grade::getNota).average().orElse(0);
            if (average >= threshold || alerts.existsByStudent_IdAndCourse_IdAndPeriodoAndEstadoNot(
                    studentId, course.getId(), course.getPeriodo(), "RESUELTA")) continue;

            var alert = AcademicRiskAlert.builder()
                    .student(student)
                    .course(course)
                    .periodo(course.getPeriodo())
                    .motivo("Promedio del curso por debajo del umbral académico configurado.")
                    .promedio(Math.round(average * 100.0) / 100.0)
                    .umbral(threshold)
                    .estado("PENDIENTE")
                    .build();
            alerts.save(alert);
        }
    }

    @Transactional(readOnly = true)
    public List<AlertDto> listForAdministrator() {
        return alerts.findAllByOrderByFechaGeneracionDesc().stream().map(AcademicRiskService::dto).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertDto> listForTeacher(String username) {
        Teacher teacher = teachers.findByUser_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado"));
        return alerts.findByCourse_Teacher_IdOrderByFechaGeneracionDesc(teacher.getId()).stream()
                .map(AcademicRiskService::dto).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertDto> listForStudent(Long studentId) {
        return alerts.findByStudent_IdOrderByFechaGeneracionDesc(studentId).stream()
                .map(AcademicRiskService::dto).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertDto> listForStudents(Collection<Long> studentIds) {
        if (studentIds.isEmpty()) return List.of();
        return alerts.findByStudent_IdInOrderByFechaGeneracionDesc(studentIds).stream()
                .map(AcademicRiskService::dto).toList();
    }

    @Transactional
    public AlertDto updateStatus(Long id, String status) {
        if (!Set.of("PENDIENTE", "ATENDIDA", "RESUELTA").contains(status))
            throw new IllegalArgumentException("Estado de alerta inválido");
        AcademicRiskAlert alert = alerts.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new IllegalArgumentException("Alerta no encontrada"));
        alert.setEstado(status);
        return dto(alerts.save(alert));
    }
}
