package pe.edu.blenkir.ssacb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;

@Configuration
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DataSeeder {

    @Bean
    CommandLineRunner seed(
            UserAccountRepository users,
            StudentRepository students,
            TeacherRepository teachers,
            GuardianRepository guardians,
            GuardianStudentRepository links,
            CourseRepository courses,
            EnrollmentRepository enrollments,
            EvaluationRepository evaluations,
            GradeRepository grades,
            AttendanceRepository attendance,
            PasswordEncoder encoder) {

        return args -> {
            if (users.count() > 0) return;

            users.save(
                    UserAccount.builder()
                            .username("admin")
                            .passwordHash(encoder.encode("admin123"))
                            .displayName("Administrador Blenkir")
                            .role(Role.ADMINISTRADOR)
                            .build()
            );

            users.save(UserAccount.builder()
                    .username("secretaria")
                    .passwordHash(encoder.encode("secretaria123"))
                    .displayName("Secretaría Blenkir")
                    .role(Role.SECRETARIA)
                    .build());

            var tu = users.save(
                    UserAccount.builder()
                            .username("docente")
                            .passwordHash(encoder.encode("docente123"))
                            .displayName("Prof. Carlos Gómez")
                            .role(Role.DOCENTE)
                            .build()
            );

            var su = users.save(
                    UserAccount.builder()
                            .username("alumno")
                            .passwordHash(encoder.encode("alumno123"))
                            .displayName("Juan Diego Pérez Ramos")
                            .role(Role.ESTUDIANTE)
                            .build()
            );

            var gu = users.save(
                    UserAccount.builder()
                            .username("apoderado")
                            .passwordHash(encoder.encode("apoderado123"))
                            .displayName("María Ramos López")
                            .role(Role.APODERADO)
                            .build()
            );

            var t = teachers.save(
                    Teacher.builder()
                            .codigo("DOC001")
                            .dni("70000001")
                            .nombres("Carlos")
                            .apellidos("Gómez")
                            .email("carlos.gomez@blenkir.edu.pe")
                            .especialidad("Matemática")
                            .user(tu)
                            .build()
            );

            var s = students.save(
                    Student.builder()
                            .codigo("EST2026001")
                            .dni("72123456")
                            .nombres("Juan Diego")
                            .apellidos("Pérez Ramos")
                            .fechaNacimiento(LocalDate.of(2010, 3, 14))
                            .grado("5° Secundaria")
                            .seccion("A")
                            .email("juan.perez@blenkir.edu.pe")
                            .user(su)
                            .build()
            );

            var g = guardians.save(
                    Guardian.builder()
                            .dni("41234567")
                            .nombres("María")
                            .apellidos("Ramos López")
                            .telefono("987111222")
                            .email("maria.ramos@gmail.com")
                            .user(gu)
                            .build()
            );

            links.save(
                    GuardianStudent.builder()
                            .guardian(g)
                            .student(s)
                            .parentesco("Madre")
                            .build()
            );

            var c = courses.save(
                    Course.builder()
                            .codigo("MAT5A")
                            .nombre("Matemática")
                            .grado("5° Secundaria")
                            .seccion("A")
                            .periodo("2026-I")
                            .teacher(t)
                            .build()
            );

            enrollments.save(
                    Enrollment.builder()
                            .student(s)
                            .course(c)
                            .build()
            );

            var ev = evaluations.save(
                    Evaluation.builder()
                            .course(c)
                            .nombre("Examen 1er bimestre")
                            .tipo("Examen")
                            .fecha(LocalDate.of(2026, 5, 28))
                            .peso(100)
                            .build()
            );

            grades.save(
                    Grade.builder()
                            .evaluation(ev)
                            .student(s)
                            .nota(16.0)
                            .observacion("Logro esperado")
                            .build()
            );

            attendance.save(
                    Attendance.builder()
                            .course(c)
                            .student(s)
                            .fecha(LocalDate.of(2026, 5, 22))
                            .estado(Attendance.Estado.PRESENTE)
                            .build()
            );

            attendance.save(
                    Attendance.builder()
                            .course(c)
                            .student(s)
                            .fecha(LocalDate.of(2026, 5, 21))
                            .estado(Attendance.Estado.PRESENTE)
                            .build()
            );

            attendance.save(
                    Attendance.builder()
                            .course(c)
                            .student(s)
                            .fecha(LocalDate.of(2026, 5, 20))
                            .estado(Attendance.Estado.TARDANZA)
                            .build()
            );
        };
    }
}
