package pe.edu.blenkir.ssacb;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AcademicRiskServiceTest {
    private final StudentRepository students = mock(StudentRepository.class);
    private final EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
    private final GradeRepository grades = mock(GradeRepository.class);
    private final AcademicRiskAlertRepository alerts = mock(AcademicRiskAlertRepository.class);
    private final TeacherRepository teachers = mock(TeacherRepository.class);
    private final AcademicRiskService service = new AcademicRiskService(
            students, enrollments, grades, alerts, teachers, 11.0);

    @Test
    void createsOneAlertForLowAverageAndDoesNotDuplicateActiveAlert() {
        Student student = Student.builder().id(1L).activo(true).build();
        Course course = Course.builder().id(2L).periodo("2026-I").nombre("Matemática").build();
        Enrollment enrollment = Enrollment.builder().student(student).course(course).activo(true).build();
        Evaluation evaluation = Evaluation.builder().course(course).build();
        Grade grade = Grade.builder().student(student).evaluation(evaluation).nota(9.0).build();
        when(students.findById(1L)).thenReturn(Optional.of(student));
        when(enrollments.findByStudent_IdAndActivoTrue(1L)).thenReturn(List.of(enrollment));
        when(grades.findByStudent_IdOrderByEvaluation_FechaDesc(1L)).thenReturn(List.of(grade));
        when(alerts.existsByStudent_IdAndCourse_IdAndPeriodoAndEstadoNot(1L, 2L, "2026-I", "RESUELTA"))
                .thenReturn(false, true);

        service.evaluateStudent(1L);
        service.evaluateStudent(1L);

        var captor = org.mockito.ArgumentCaptor.forClass(AcademicRiskAlert.class);
        verify(alerts, times(1)).save(captor.capture());
        assertEquals(9.0, captor.getValue().getPromedio());
        assertEquals("PENDIENTE", captor.getValue().getEstado());
    }

    @Test
    void doesNotAlertAtTheThreshold() {
        Student student = Student.builder().id(1L).activo(true).build();
        Course course = Course.builder().id(2L).periodo("2026-I").build();
        Enrollment enrollment = Enrollment.builder().student(student).course(course).activo(true).build();
        Evaluation evaluation = Evaluation.builder().course(course).build();
        Grade grade = Grade.builder().student(student).evaluation(evaluation).nota(11.0).build();
        when(students.findById(1L)).thenReturn(Optional.of(student));
        when(enrollments.findByStudent_IdAndActivoTrue(1L)).thenReturn(List.of(enrollment));
        when(grades.findByStudent_IdOrderByEvaluation_FechaDesc(1L)).thenReturn(List.of(grade));

        service.evaluateStudent(1L);

        verify(alerts, never()).save(any());
    }
}
