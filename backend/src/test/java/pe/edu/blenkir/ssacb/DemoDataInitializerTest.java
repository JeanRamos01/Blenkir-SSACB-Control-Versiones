package pe.edu.blenkir.ssacb;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DemoDataInitializerTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final StudentRepository students = mock(StudentRepository.class);
    private final TeacherRepository teachers = mock(TeacherRepository.class);
    private final GuardianRepository guardians = mock(GuardianRepository.class);
    private final GuardianStudentRepository links = mock(GuardianStudentRepository.class);
    private final CourseRepository courses = mock(CourseRepository.class);
    private final EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
    private final EvaluationRepository evaluations = mock(EvaluationRepository.class);
    private final GradeRepository grades = mock(GradeRepository.class);
    private final AttendanceRepository attendance = mock(AttendanceRepository.class);
    private final AcademicRiskService risk = mock(AcademicRiskService.class);
    private final AcademicRiskAlertRepository riskAlerts = mock(AcademicRiskAlertRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final DemoDataInitializer initializer = new DemoDataInitializer(jdbc, users, students, teachers,
            guardians, links, courses, enrollments, evaluations, grades, attendance, risk, riskAlerts,
            encoder, "", "", "", "", "");

    @Test
    void doesNotSeedWhenAnyApplicationDataAlreadyExists() {
        when(users.findByUsername(anyString())).thenReturn(Optional.empty());
        when(jdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(1L);

        initializer.initialize();

        verify(users, never()).save(any());
        verify(students, never()).save(any());
        verify(teachers, never()).save(any());
        verify(guardians, never()).save(any());
        verify(evaluations, never()).save(any());
        verify(grades, never()).save(any());
    }
}
