package pe.edu.blenkir.ssacb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class DemoDataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final String DEMO_ADMIN = "demo-admin";
    private static final String DEMO_SECRETARIA = "demo-secretaria";
    private static final String DEMO_TEACHER = "demo-docente";
    private static final String DEMO_STUDENT = "demo-estudiante-riesgo";
    private static final String DEMO_GUARDIAN = "demo-apoderado";
    private static final String DEMO_TEACHER_CODE = "DEMO-DOC-001";
    private static final String DEMO_TEACHER_DNI = "99990001";
    private static final String DEMO_STUDENT_CODE = "DEMO-EST-001";
    private static final String DEMO_STUDENT_DNI = "99990002";
    private static final String DEMO_GUARDIAN_DNI = "99990003";
    private static final String DEMO_COURSE_CODE = "DEMO-MAT-2026";

    private final JdbcTemplate jdbc;
    private final UserAccountRepository users;
    private final StudentRepository students;
    private final TeacherRepository teachers;
    private final GuardianRepository guardians;
    private final GuardianStudentRepository links;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final EvaluationRepository evaluations;
    private final GradeRepository grades;
    private final AttendanceRepository attendance;
    private final AcademicRiskService risk;
    private final AcademicRiskAlertRepository riskAlerts;
    private final PasswordEncoder encoder;
    private final String adminPassword;
    private final String secretariatPassword;
    private final String teacherPassword;
    private final String studentPassword;
    private final String guardianPassword;
    private final boolean allowExistingData;

    public DemoDataInitializer(JdbcTemplate jdbc, UserAccountRepository users, StudentRepository students,
            TeacherRepository teachers, GuardianRepository guardians, GuardianStudentRepository links,
            CourseRepository courses, EnrollmentRepository enrollments, EvaluationRepository evaluations,
            GradeRepository grades, AttendanceRepository attendance, AcademicRiskService risk,
            AcademicRiskAlertRepository riskAlerts, PasswordEncoder encoder,
            @Value("${app.demo.admin-password:}") String adminPassword,
            @Value("${app.demo.secretariat-password:}") String secretariatPassword,
            @Value("${app.demo.teacher-password:}") String teacherPassword,
            @Value("${app.demo.student-password:}") String studentPassword,
            @Value("${app.demo.guardian-password:}") String guardianPassword,
            @Value("${app.demo.allow-existing-data:false}") boolean allowExistingData) {
        this.jdbc = jdbc;
        this.users = users;
        this.students = students;
        this.teachers = teachers;
        this.guardians = guardians;
        this.links = links;
        this.courses = courses;
        this.enrollments = enrollments;
        this.evaluations = evaluations;
        this.grades = grades;
        this.attendance = attendance;
        this.risk = risk;
        this.riskAlerts = riskAlerts;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
        this.secretariatPassword = secretariatPassword;
        this.teacherPassword = teacherPassword;
        this.studentPassword = studentPassword;
        this.guardianPassword = guardianPassword;
        this.allowExistingData = allowExistingData;
    }

    @Transactional
    public void initialize() {
        jdbc.execute("SELECT pg_advisory_xact_lock(26061029, 2)");
        if (isCompleteDemoDataset()) {
            log.info("Conjunto DEMO ya inicializado; se conservan cuentas y datos existentes.");
            return;
        }
        if (hasAnyDemoAccount()) {
            log.warn("Inicialización DEMO omitida: ya existe una cuenta DEMO, pero el conjunto está incompleto; no se modifican datos.");
            return;
        }
        if (!allowExistingData && hasAnyApplicationData()) {
            log.warn("Inicialización DEMO omitida: la base no está vacía o contiene una inicialización parcial.");
            return;
        }
        if (allowExistingData) {
            log.warn("DEMO_ALLOW_EXISTING_DATA=true: se agregará el conjunto DEMO sin modificar los registros existentes.");
        }
        if (hasDemoIdentityConflict()) {
            log.warn("Inicialización DEMO omitida: uno o más identificadores reservados DEMO ya pertenecen a otros registros.");
            return;
        }

        requirePassword(adminPassword, "DEMO_ADMIN_PASSWORD");
        requirePassword(secretariatPassword, "DEMO_SECRETARIA_PASSWORD");
        requirePassword(teacherPassword, "DEMO_DOCENTE_PASSWORD");
        requirePassword(studentPassword, "DEMO_ESTUDIANTE_PASSWORD");
        requirePassword(guardianPassword, "DEMO_APODERADO_PASSWORD");

        users.save(account(DEMO_ADMIN, "Administrador DEMO", Role.ADMINISTRADOR, adminPassword));
        users.save(account(DEMO_SECRETARIA, "Secretaría DEMO", Role.SECRETARIA, secretariatPassword));
        var teacherAccount = users.save(account(DEMO_TEACHER, "Docente DEMO", Role.DOCENTE, teacherPassword));
        var studentAccount = users.save(account(DEMO_STUDENT, "Estudiante DEMO Riesgo", Role.ESTUDIANTE, studentPassword));
        var guardianAccount = users.save(account(DEMO_GUARDIAN, "Apoderado DEMO", Role.APODERADO, guardianPassword));

        var teacher = teachers.save(Objects.requireNonNull(Teacher.builder()
                .codigo(DEMO_TEACHER_CODE).dni(DEMO_TEACHER_DNI).nombres("Docente").apellidos("DEMO")
                .email("demo-docente@example.invalid").especialidad("Matemática")
                .user(teacherAccount).build()));
        var student = students.save(Objects.requireNonNull(Student.builder()
                .codigo(DEMO_STUDENT_CODE).dni(DEMO_STUDENT_DNI).nombres("Estudiante")
                .apellidos("DEMO Riesgo Académico").fechaNacimiento(LocalDate.of(2010, 3, 14))
                .grado("5° Secundaria").seccion("A").email("demo-estudiante@example.invalid")
                .user(studentAccount).build()));
        var guardian = guardians.save(Objects.requireNonNull(Guardian.builder()
                .dni(DEMO_GUARDIAN_DNI).nombres("Apoderado").apellidos("DEMO")
                .telefono("900000003").email("demo-apoderado@example.invalid")
                .user(guardianAccount).build()));
        links.save(Objects.requireNonNull(GuardianStudent.builder()
                .guardian(guardian).student(student).parentesco("Apoderado DEMO").autorizado(true).build()));

        var course = courses.save(Objects.requireNonNull(Course.builder()
                .codigo(DEMO_COURSE_CODE).nombre("Matemática DEMO").grado("5° Secundaria")
                .seccion("A").periodo("DEMO-2026").teacher(teacher).build()));
        enrollments.save(Objects.requireNonNull(Enrollment.builder().student(student).course(course).activo(true).build()));

        var first = evaluations.save(Objects.requireNonNull(Evaluation.builder().course(course)
                .nombre("Evaluación DEMO 1").tipo("Prueba").fecha(LocalDate.now().minusDays(14)).peso(50).build()));
        var second = evaluations.save(Objects.requireNonNull(Evaluation.builder().course(course)
                .nombre("Evaluación DEMO 2").tipo("Prueba").fecha(LocalDate.now().minusDays(7)).peso(50).build()));
        grades.save(Objects.requireNonNull(Grade.builder().evaluation(first).student(student).nota(8.0)
                .observacion("Calificación de prueba DEMO").build()));
        grades.save(Objects.requireNonNull(Grade.builder().evaluation(second).student(student).nota(10.0)
                .observacion("Calificación de prueba DEMO").build()));
        attendance.save(Objects.requireNonNull(Attendance.builder().course(course).student(student)
                .fecha(LocalDate.now().minusDays(1)).estado(Attendance.Estado.PRESENTE).build()));

        risk.evaluateStudent(student.getId());
        log.info("Datos DEMO creados; no se registran contraseñas en logs.");
    }

    private boolean hasAnyDemoAccount() {
        return users.findByUsername(DEMO_ADMIN).isPresent()
                || users.findByUsername(DEMO_SECRETARIA).isPresent()
                || users.findByUsername(DEMO_TEACHER).isPresent()
                || users.findByUsername(DEMO_STUDENT).isPresent()
                || users.findByUsername(DEMO_GUARDIAN).isPresent();
    }

    private boolean hasDemoIdentityConflict() {
        return teachers.findByCodigo(DEMO_TEACHER_CODE).isPresent()
                || teachers.findByDni(DEMO_TEACHER_DNI).isPresent()
                || students.findByCodigo(DEMO_STUDENT_CODE).isPresent()
                || students.findByDni(DEMO_STUDENT_DNI).isPresent()
                || guardians.findByDni(DEMO_GUARDIAN_DNI).isPresent()
                || courses.findByCodigo(DEMO_COURSE_CODE).isPresent();
    }

    private UserAccount account(String username, String displayName, Role role, String password) {
        return Objects.requireNonNull(UserAccount.builder().username(username)
                .passwordHash(encoder.encode(password)).displayName(displayName).role(role).build());
    }

    private void requirePassword(String password, String variable) {
        if (password == null || password.length() < 12)
            throw new IllegalStateException(variable + " debe configurarse con al menos 12 caracteres cuando DEMO_DATA_ENABLED=true.");
        if (password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalStateException(variable + " supera el máximo de 72 bytes permitido por BCrypt.");
    }

    private boolean isCompleteDemoDataset() {
        var admin = users.findByUsername(DEMO_ADMIN);
        var secretary = users.findByUsername(DEMO_SECRETARIA);
        var teacherUser = users.findByUsername(DEMO_TEACHER);
        var studentUser = users.findByUsername(DEMO_STUDENT);
        var guardianUser = users.findByUsername(DEMO_GUARDIAN);
        if (admin.isEmpty() || secretary.isEmpty() || teacherUser.isEmpty()
                || studentUser.isEmpty() || guardianUser.isEmpty()) return false;
        if (admin.get().getRole() != Role.ADMINISTRADOR || secretary.get().getRole() != Role.SECRETARIA
                || teacherUser.get().getRole() != Role.DOCENTE || studentUser.get().getRole() != Role.ESTUDIANTE
                || guardianUser.get().getRole() != Role.APODERADO) return false;
        var student = students.findByUser_Username(DEMO_STUDENT);
        var teacher = teachers.findByUser_Username(DEMO_TEACHER);
        var guardian = guardians.findByUser_Username(DEMO_GUARDIAN);
        if (student.isEmpty() || teacher.isEmpty() || guardian.isEmpty()) return false;
        var course = courses.findByCodigo(DEMO_COURSE_CODE);
        return course.isPresent() && course.get().getTeacher() != null
                && Objects.equals(course.get().getTeacher().getId(), teacher.get().getId())
                && enrollments.findByStudent_IdAndCourse_Id(student.get().getId(), course.get().getId()).isPresent()
                && !grades.findByStudent_IdOrderByEvaluation_FechaDesc(student.get().getId()).isEmpty()
                && links.findByGuardian_IdAndStudent_IdAndAutorizadoTrue(guardian.get().getId(), student.get().getId()).isPresent()
                && !riskAlerts.findByStudent_IdOrderByFechaGeneracionDesc(student.get().getId()).isEmpty();
    }

    private boolean hasAnyApplicationData() {
        Long count = jdbc.queryForObject("""
                SELECT (SELECT COUNT(*) FROM usuarios)
                     + (SELECT COUNT(*) FROM estudiantes)
                     + (SELECT COUNT(*) FROM docentes)
                     + (SELECT COUNT(*) FROM apoderados)
                     + (SELECT COUNT(*) FROM apoderado_estudiante)
                     + (SELECT COUNT(*) FROM cursos)
                     + (SELECT COUNT(*) FROM matriculas_curso)
                     + (SELECT COUNT(*) FROM evaluaciones)
                     + (SELECT COUNT(*) FROM calificaciones)
                     + (SELECT COUNT(*) FROM asistencias)
                     + (SELECT COUNT(*) FROM horarios_clases)
                     + (SELECT COUNT(*) FROM personal_colegio)
                     + (SELECT COUNT(*) FROM conceptos_cobro_blenkir)
                     + (SELECT COUNT(*) FROM cuentas_por_cobrar)
                     + (SELECT COUNT(*) FROM pagos_blenkir)
                     + (SELECT COUNT(*) FROM movimientos_caja_blenkir)
                     + (SELECT COUNT(*) FROM comunicados_blenkir)
                     + (SELECT COUNT(*) FROM auditoria_blenkir)
                     + (SELECT COUNT(*) FROM competencias_blenkir)
                     + (SELECT COUNT(*) FROM logros_competencias_blenkir)
                     + (SELECT COUNT(*) FROM alertas_academicas)
                """, Long.class);
        return count != null && count > 0;
    }
}
