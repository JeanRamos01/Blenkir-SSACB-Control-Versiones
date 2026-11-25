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
            encoder, "", "", "", "", "", false);

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

    @Test
    void doesNotSeedWhenDemoAccountAlreadyExistsButDatasetIsIncomplete() {
        when(users.findByUsername("demo-admin")).thenReturn(Optional.of(UserAccount.builder()
                .username("demo-admin").role(Role.ADMINISTRADOR).build()));

        initializer.initialize();

        verify(users, never()).save(any());
        verify(students, never()).save(any());
        verify(teachers, never()).save(any());
        verify(guardians, never()).save(any());
    }

    @Test
    void seedsAlongsideExistingApplicationDataWhenExplicitlyAllowed() {
        when(users.findByUsername(anyString())).thenReturn(Optional.empty());
        when(jdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(1L);
        when(students.findByDni(anyString())).thenReturn(Optional.empty());
        when(students.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(teachers.findByDni(anyString())).thenReturn(Optional.empty());
        when(teachers.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(guardians.findByDni(anyString())).thenReturn(Optional.empty());
        when(courses.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(encoder.encode(anyString())).thenReturn("encoded-demo-password");
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(teachers.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(students.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(guardians.save(any(Guardian.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(links.save(any(GuardianStudent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courses.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollments.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(evaluations.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(grades.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendance.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var populatedDatabaseInitializer = new DemoDataInitializer(jdbc, users, students, teachers,
                guardians, links, courses, enrollments, evaluations, grades, attendance, risk, riskAlerts,
                encoder, "AdminDemoPassword123", "SecretariaDemo123", "DocenteDemoPassword123",
                "EstudianteDemo123", "ApoderadoDemo123", true);

        populatedDatabaseInitializer.initialize();

        verify(users, times(5)).save(any(UserAccount.class));
        verify(teachers).save(any(Teacher.class));
        verify(students).save(any(Student.class));
        verify(guardians).save(any(Guardian.class));
        verify(courses).save(any(Course.class));
        verify(enrollments).save(any(Enrollment.class));
        verify(evaluations, times(2)).save(any(Evaluation.class));
        verify(grades, times(2)).save(any(Grade.class));
        verify(attendance).save(any(Attendance.class));
        verify(risk).evaluateStudent(null);
    }

    @Test
    void doesNotSeedWhenReservedDemoIdentifierIsAlreadyUsed() {
        when(users.findByUsername(anyString())).thenReturn(Optional.empty());
        when(students.findByDni("99990002")).thenReturn(Optional.of(Student.builder().dni("99990002").build()));
        var populatedDatabaseInitializer = new DemoDataInitializer(jdbc, users, students, teachers,
                guardians, links, courses, enrollments, evaluations, grades, attendance, risk, riskAlerts,
                encoder, "AdminDemoPassword123", "SecretariaDemo123", "DocenteDemoPassword123",
                "EstudianteDemo123", "ApoderadoDemo123", true);

        populatedDatabaseInitializer.initialize();

        verify(users, never()).save(any());
        verify(students, never()).save(any());
        verify(teachers, never()).save(any());
        verify(guardians, never()).save(any());
    }
}
