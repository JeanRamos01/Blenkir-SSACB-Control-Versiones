package pe.edu.blenkir.ssacb;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TestDataLoader {
    private static final String MARKER = "TST26";
    private static final String PERIOD = "TST26-2026-II";
    private static final int STUDENT_COUNT = 200;
    private static final int TEACHER_COUNT = 20;
    private static final int COURSE_COUNT = 13;
    private static final int STAFF_COUNT = 10;
    private static final int ATTENDANCE_DAY = 15;
    private static final LocalDate ACADEMIC_DATE = LocalDate.of(2026, 10, ATTENDANCE_DAY);
    private static final LocalDateTime SEED_TIMESTAMP = LocalDateTime.of(2026, 10, ATTENDANCE_DAY, 12, 0);

    private static final Map<String, List<String>> REQUIRED_COLUMNS = requiredColumns();
    private static final String[] SUBJECTS = {
            "Matemática", "Comunicación", "Ciencia y Tecnología", "Ciencias Sociales",
            "Inglés", "Educación Física", "Arte y Cultura", "Educación para el Trabajo",
            "Desarrollo Personal", "Educación Religiosa", "Álgebra", "Geometría", "Lectura"
    };
    private static final String[] FIRST_NAMES = {
            "Adriana", "Bruno", "Camila", "Diego", "Elena", "Fabio", "Gabriela", "Hugo",
            "Inés", "Javier", "Kiara", "Leonardo", "Mariana", "Nicolás", "Olivia", "Pablo",
            "Renata", "Sergio", "Tamara", "Víctor"
    };
    private static final String[] LAST_NAMES = {
            "Álvarez", "Benites", "Castro", "Díaz", "Espinoza", "Flores", "García", "Herrera",
            "Ibarra", "Jiménez", "Luna", "Mendoza", "Navarro", "Ortega", "Paredes", "Quispe",
            "Ramírez", "Salazar", "Torres", "Vargas"
    };

    private TestDataLoader() {
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            String mode = parseMode(args);
            if (!mode.equals("plan") && !mode.equals("apply") && !mode.equals("clean")) {
                throw new IllegalArgumentException("Modo inválido. Use --mode=plan, --mode=apply o --mode=clean.");
            }
            if (!mode.equals("plan") && !"YES".equals(System.getenv("TEST_DATA_APPROVED"))) {
                throw new IllegalStateException(
                        "Para modificar la base, configure TEST_DATA_APPROVED=YES después de revisar y aprobar el plan.");
            }

            String jdbcUrl = envOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/blenkir_ssacb");
            String dbUser = envOrDefault("DB_USERNAME", "blenkir_app");
            String dbPassword = System.getenv("DB_PASSWORD");
            if (dbPassword == null || dbPassword.isBlank()) {
                throw new IllegalStateException("Configure DB_PASSWORD en el entorno; no se usa una contraseña por defecto.");
            }

            try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
                connection.setAutoCommit(false);
                try {
                    validateSchema(connection);
                    if (mode.equals("plan")) {
                        printPlan(connection);
                        connection.rollback();
                    } else if (mode.equals("apply")) {
                        String testPassword = System.getenv("TEST_DATA_PASSWORD");
                        if (testPassword == null || testPassword.length() < 12) {
                            throw new IllegalStateException(
                                    "Configure TEST_DATA_PASSWORD con al menos 12 caracteres para generar cuentas ficticias.");
                        }
                        lockSeeder(connection);
                        seed(connection, new BCryptPasswordEncoder().encode(testPassword));
                        connection.commit();
                        System.out.println("Carga de datos de prueba confirmada.");
                    } else {
                        lockSeeder(connection);
                        clean(connection);
                        connection.commit();
                        System.out.println("Limpieza de registros de prueba confirmada.");
                    }
                } catch (Exception exception) {
                    connection.rollback();
                    throw exception;
                }
            }
        } catch (Exception exception) {
            System.err.println("No se completó el proceso: " + exception.getMessage());
            exception.printStackTrace(System.err);
            exitCode = 1;
        }
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static String parseMode(String[] args) {
        if (args.length == 0) {
            return "plan";
        }
        if (args.length == 1 && args[0].startsWith("--mode=")) {
            return args[0].substring("--mode=".length());
        }
        throw new IllegalArgumentException("Argumentos esperados: --mode=plan|apply|clean.");
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static void validateSchema(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        String schema = connection.getSchema();
        List<String> problems = new ArrayList<>();

        for (Map.Entry<String, List<String>> table : REQUIRED_COLUMNS.entrySet()) {
            Map<String, Integer> actualColumns = new HashMap<>();
            try (ResultSet columns = metadata.getColumns(connection.getCatalog(), schema, table.getKey(), null)) {
                while (columns.next()) {
                    actualColumns.put(columns.getString("COLUMN_NAME").toLowerCase(), columns.getInt("DATA_TYPE"));
                }
            }
            if (actualColumns.isEmpty()) {
                problems.add("falta la tabla " + table.getKey());
                continue;
            }
            for (String required : table.getValue()) {
                Integer actualType = actualColumns.get(required);
                if (actualType == null) {
                    problems.add(table.getKey() + "." + required + " no existe");
                } else if (!typeMatches(expectedSqlType(table.getKey(), required), actualType)) {
                    problems.add(table.getKey() + "." + required + " tiene un tipo SQL incompatible");
                }
            }
        }

        Map<String, Set<String>> primaryKeys = primaryKeys(metadata, connection, schema);
        for (String table : REQUIRED_COLUMNS.keySet()) {
            if (!primaryKeys.getOrDefault(table, Set.of()).equals(Set.of("id"))) {
                problems.add(table + " debe tener id como clave primaria");
            }
        }

        Map<String, Set<Set<String>>> uniqueIndexes = uniqueIndexes(metadata, connection, schema);
        expectedUniqueConstraints().forEach((table, expected) -> {
            Set<Set<String>> actual = uniqueIndexes.getOrDefault(table, Set.of());
            for (Set<String> key : expected) {
                if (!actual.contains(key)) {
                    problems.add(table + " no tiene UNIQUE " + key);
                }
            }
        });

        Map<String, Set<String>> foreignKeys = foreignKeys(metadata, connection, schema);
        expectedForeignKeys().forEach((table, expected) -> {
            Set<String> actual = foreignKeys.getOrDefault(table, Set.of());
            for (String key : expected) {
                if (!actual.contains(key)) {
                    problems.add(table + " no tiene FK " + key);
                }
            }
        });

        if (!problems.isEmpty()) {
            throw new IllegalStateException("El esquema no coincide con las entidades SQL/JPA o sus restricciones: "
                    + String.join("; ", problems));
        }
        System.out.println("Validación de esquema: OK (20 tablas, columnas, PK, UNIQUE y FK requeridas).");
    }

    private static int expectedSqlType(String table, String column) {
        if (column.equals("id") || column.endsWith("_id") || column.equals("version")) {
            return Types.BIGINT;
        }
        if (column.equals("activo") || column.equals("autorizado")) {
            return Types.BOOLEAN;
        }
        if (column.equals("importe") || column.equals("pagado")) {
            return Types.NUMERIC;
        }
        if (column.equals("nota")) {
            return Types.DOUBLE;
        }
        if (column.equals("fecha_nacimiento") || column.equals("vencimiento")
                || (column.equals("fecha") && (table.equals("evaluaciones") || table.equals("asistencias")))) {
            return Types.DATE;
        }
        if (column.equals("actualizado_en") || (column.equals("fecha")
                && (table.equals("pagos_blenkir") || table.equals("movimientos_caja_blenkir")
                || table.equals("comunicados_blenkir") || table.equals("auditoria_blenkir")))) {
            return Types.TIMESTAMP;
        }
        if (column.equals("hora_inicio") || column.equals("hora_fin")) {
            return Types.TIME;
        }
        if (column.equals("peso") || column.equals("dia_semana")) {
            return Types.INTEGER;
        }
        return Types.VARCHAR;
    }

    private static boolean typeMatches(int expected, int actual) {
        if (expected == Types.NUMERIC) {
            return actual == Types.NUMERIC || actual == Types.DECIMAL;
        }
        if (expected == Types.BOOLEAN) {
            return actual == Types.BOOLEAN || actual == Types.BIT;
        }
        return expected == actual;
    }

    private static void printPlan(Connection connection) throws SQLException {
        Map<String, Integer> planned = plannedRowCounts();
        Map<String, Long> existing = seedRowCounts(connection);
        System.out.println("Modo PLAN: solo lectura; no se ejecutarán inserciones ni cambios.");
        System.out.println("Base conectada: " + connection.getMetaData().getDatabaseProductName()
                + ", esquema " + connection.getSchema() + ".");
        System.out.println("Datos sintéticos reservados bajo el prefijo " + MARKER + ".");
        System.out.println("Contraseñas de las cuentas: BCryptPasswordEncoder de Spring Security.");
        System.out.println("Filas objetivo / ya marcadas / estimación de filas nuevas:");
        planned.forEach((table, count) -> {
            long found = existing.get(table);
            if (found > count) {
                throw new IllegalStateException("Hay más filas marcadas que las previstas en " + table
                        + "; revise el marcador reservado antes de continuar.");
            }
            System.out.printf("  %-32s %5d / %5d / %5d%n", table, count, found, count - found);
        });
        System.out.println("Total objetivo: " + planned.values().stream().mapToInt(Integer::intValue).sum());
        System.out.println("Cada inserción omite conflictos UNIQUE existentes; ningún registro existente se actualiza.");
        System.out.println("La carga completa se ejecutará dentro de una transacción y se revierte ante un error.");
    }

    private static Map<String, Integer> plannedRowCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("usuarios", STUDENT_COUNT * 2 + TEACHER_COUNT);
        counts.put("docentes", TEACHER_COUNT);
        counts.put("estudiantes", STUDENT_COUNT);
        counts.put("apoderados", STUDENT_COUNT);
        counts.put("apoderado_estudiante", STUDENT_COUNT);
        counts.put("personal_colegio", STAFF_COUNT);
        counts.put("cursos", COURSE_COUNT);
        counts.put("matriculas_curso", STUDENT_COUNT * COURSE_COUNT);
        counts.put("evaluaciones", COURSE_COUNT);
        counts.put("calificaciones", STUDENT_COUNT * COURSE_COUNT);
        counts.put("asistencias", STUDENT_COUNT * COURSE_COUNT);
        counts.put("horarios_clases", COURSE_COUNT);
        counts.put("competencias_blenkir", COURSE_COUNT);
        counts.put("logros_competencias_blenkir", STUDENT_COUNT * COURSE_COUNT);
        counts.put("conceptos_cobro_blenkir", 2);
        counts.put("cuentas_por_cobrar", STUDENT_COUNT * 2);
        counts.put("pagos_blenkir", STUDENT_COUNT / 2);
        counts.put("movimientos_caja_blenkir", STUDENT_COUNT / 2);
        counts.put("comunicados_blenkir", 3);
        counts.put("auditoria_blenkir", 3);
        return counts;
    }

    private static void lockSeeder(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT pg_advisory_xact_lock(26061029, 1)")) {
            statement.execute();
        }
    }

    private static void seed(Connection connection, String passwordHash) throws SQLException {
        System.out.println("Inserción protegida por bloqueo transaccional e idempotencia.");
        seedUsers(connection, passwordHash);
        seedTeachers(connection);
        seedStudents(connection);
        seedGuardians(connection);
        seedStaff(connection);
        seedCourses(connection);
        seedFamilyLinks(connection);
        seedEnrollments(connection);
        seedSchedules(connection);
        seedEvaluations(connection);
        seedCompetencies(connection);
        seedAttendanceAndGrades(connection);
        seedCompetencyResults(connection);
        seedFinance(connection);
        seedNoticesAndAudit(connection);
        verifySeedCounts(connection);
    }

    private static void seedUsers(Connection connection, String passwordHash) throws SQLException {
        for (int index = 1; index <= STUDENT_COUNT; index++) {
            String studentName = fullName(index);
            insert(connection,
                    "INSERT INTO usuarios (username,password_hash,nombre_mostrado,role,activo) "
                            + "VALUES (?,?,?,?,TRUE) ON CONFLICT DO NOTHING",
                    "test_seed_26_student_" + four(index), passwordHash, studentName, "ESTUDIANTE");
            String guardianName = guardianName(index);
            insert(connection,
                    "INSERT INTO usuarios (username,password_hash,nombre_mostrado,role,activo) "
                            + "VALUES (?,?,?,?,TRUE) ON CONFLICT DO NOTHING",
                    "test_seed_26_guardian_" + four(index), passwordHash, guardianName, "APODERADO");
        }
        for (int index = 1; index <= TEACHER_COUNT; index++) {
            insert(connection,
                    "INSERT INTO usuarios (username,password_hash,nombre_mostrado,role,activo) "
                            + "VALUES (?,?,?,?,TRUE) ON CONFLICT DO NOTHING",
                    "test_seed_26_teacher_" + three(index), passwordHash,
                    "Docente " + fullName(index), "DOCENTE");
        }
    }

    private static void seedTeachers(Connection connection) throws SQLException {
        for (int index = 1; index <= TEACHER_COUNT; index++) {
            insert(connection,
                    "INSERT INTO docentes (codigo,dni,nombres,apellidos,email,especialidad,activo,usuario_id) "
                            + "VALUES (?,?,?,?,?,?,TRUE,(SELECT id FROM usuarios WHERE username=?)) "
                            + "ON CONFLICT DO NOTHING",
                    "TST26-D" + three(index), dni(98_000_000 + index),
                    firstName(index), lastName(index), "docente" + three(index) + "@example.test",
                    SUBJECTS[(index - 1) % SUBJECTS.length],
                    "test_seed_26_teacher_" + three(index));
        }
    }

    private static void seedStudents(Connection connection) throws SQLException {
        for (int index = 1; index <= STUDENT_COUNT; index++) {
            insert(connection,
                    "INSERT INTO estudiantes "
                            + "(codigo,dni,nombres,apellidos,fecha_nacimiento,grado,seccion,email,direccion,telefono,activo,usuario_id) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,TRUE,(SELECT id FROM usuarios WHERE username=?)) "
                            + "ON CONFLICT DO NOTHING",
                    "TST26-S" + four(index), dni(97_000_000 + index),
                    firstName(index), lastName(index), LocalDate.of(2009 + index % 4, 1 + index % 12, 1 + index % 27),
                    "5to Secundaria", "A", "alumno" + four(index) + "@example.test",
                    "Dirección ficticia " + index, "900" + String.format("%06d", index),
                    "test_seed_26_student_" + four(index));
        }
    }

    private static void seedGuardians(Connection connection) throws SQLException {
        for (int index = 1; index <= STUDENT_COUNT; index++) {
            insert(connection,
                    "INSERT INTO apoderados (dni,nombres,apellidos,telefono,email,activo,usuario_id) "
                            + "VALUES (?,?,?,?,?,TRUE,(SELECT id FROM usuarios WHERE username=?)) "
                            + "ON CONFLICT DO NOTHING",
                    dni(96_000_000 + index), firstName(index + 7), lastName(index + 11),
                    "910" + String.format("%06d", index), "apoderado" + four(index) + "@example.test",
                    "test_seed_26_guardian_" + four(index));
        }
    }

    private static void seedStaff(Connection connection) throws SQLException {
        String[] roles = {"Secretaría", "Biblioteca", "Administración", "Auxiliar", "Tesorería"};
        for (int index = 1; index <= STAFF_COUNT; index++) {
            insert(connection,
                    "INSERT INTO personal_colegio (dni,nombre_completo,cargo,correo,telefono,activo) "
                            + "VALUES (?,?,?,?,?,TRUE) ON CONFLICT DO NOTHING",
                    dni(95_000_000 + index), fullName(index + 23),
                    roles[(index - 1) % roles.length],
                    "personal" + two(index) + "@example.test",
                    "920" + String.format("%06d", index));
        }
    }

    private static void seedCourses(Connection connection) throws SQLException {
        for (int index = 1; index <= COURSE_COUNT; index++) {
            insert(connection,
                    "INSERT INTO cursos (codigo,nombre,grado,seccion,periodo,docente_id,activo) "
                            + "VALUES (?,?,?,?,'2026-II',(SELECT id FROM docentes WHERE codigo=?),TRUE) "
                            + "ON CONFLICT DO NOTHING",
                    "TST26-C" + two(index), SUBJECTS[index - 1], "5to Secundaria", "A",
                    "TST26-D" + three(index));
        }
    }

    private static void seedFamilyLinks(Connection connection) throws SQLException {
        for (int index = 1; index <= STUDENT_COUNT; index++) {
            insert(connection,
                    "INSERT INTO apoderado_estudiante (apoderado_id,estudiante_id,parentesco,autorizado) "
                            + "SELECT a.id,e.id,'Apoderado de prueba',TRUE FROM apoderados a "
                            + "JOIN estudiantes e ON e.codigo=? WHERE a.dni=? ON CONFLICT DO NOTHING",
                    "TST26-S" + four(index), dni(96_000_000 + index));
        }
    }

    private static void seedEnrollments(Connection connection) throws SQLException {
        for (int student = 1; student <= STUDENT_COUNT; student++) {
            for (int course = 1; course <= COURSE_COUNT; course++) {
                insert(connection,
                        "INSERT INTO matriculas_curso (estudiante_id,curso_id,activo) "
                                + "SELECT e.id,c.id,TRUE FROM estudiantes e JOIN cursos c ON c.codigo=? "
                                + "WHERE e.codigo=? ON CONFLICT DO NOTHING",
                        "TST26-C" + two(course), "TST26-S" + four(student));
            }
        }
    }

    private static void seedSchedules(Connection connection) throws SQLException {
        for (int index = 1; index <= COURSE_COUNT; index++) {
            LocalTime start = LocalTime.of(8 + (index - 1) % 5, 0);
            LocalTime end = start.plusHours(1);
            insertIfMissing(connection,
                    "INSERT INTO horarios_clases (curso_id,dia_semana,hora_inicio,hora_fin,aula) "
                            + "SELECT id,?,?,?,? FROM cursos WHERE codigo=?",
                    "SELECT EXISTS(SELECT 1 FROM horarios_clases h JOIN cursos c ON c.id=h.curso_id "
                            + "WHERE c.codigo=? AND h.dia_semana=? AND h.hora_inicio=? AND h.hora_fin=?)",
                    new Object[]{(index - 1) % 7 + 1, start, end, "Aula de prueba " + two(index),
                            "TST26-C" + two(index)},
                    new Object[]{"TST26-C" + two(index), (index - 1) % 7 + 1, start, end});
        }
    }

    private static void seedEvaluations(Connection connection) throws SQLException {
        for (int index = 1; index <= COURSE_COUNT; index++) {
            String title = MARKER + " Evaluación " + two(index);
            insertIfMissing(connection,
                    "INSERT INTO evaluaciones (curso_id,nombre,tipo,fecha,peso) "
                            + "SELECT id,?,'Prueba',?,100 FROM cursos WHERE codigo=?",
                    "SELECT EXISTS(SELECT 1 FROM evaluaciones v JOIN cursos c ON c.id=v.curso_id "
                            + "WHERE c.codigo=? AND v.nombre=?)",
                    new Object[]{title, ACADEMIC_DATE, "TST26-C" + two(index)},
                    new Object[]{"TST26-C" + two(index), title});
        }
    }

    private static void seedCompetencies(Connection connection) throws SQLException {
        for (int index = 1; index <= COURSE_COUNT; index++) {
            String name = MARKER + " Competencia " + two(index);
            insertIfMissing(connection,
                    "INSERT INTO competencias_blenkir (curso_id,nombre,descripcion) "
                            + "SELECT id,?,? FROM cursos WHERE codigo=?",
                    "SELECT EXISTS(SELECT 1 FROM competencias_blenkir b JOIN cursos c ON c.id=b.curso_id "
                            + "WHERE c.codigo=? AND b.nombre=?)",
                    new Object[]{name, "Competencia ficticia para pruebas de carga.", "TST26-C" + two(index)},
                    new Object[]{"TST26-C" + two(index), name});
        }
    }

    private static void seedAttendanceAndGrades(Connection connection) throws SQLException {
        for (int student = 1; student <= STUDENT_COUNT; student++) {
            for (int course = 1; course <= COURSE_COUNT; course++) {
                String courseCode = "TST26-C" + two(course);
                String studentCode = "TST26-S" + four(student);
                int score = 10 + (student + course) % 11;
                insert(connection,
                        "INSERT INTO asistencias (curso_id,estudiante_id,fecha,estado,observacion) "
                                + "SELECT c.id,e.id,?,'PRESENTE',? FROM cursos c CROSS JOIN estudiantes e "
                                + "WHERE c.codigo=? AND e.codigo=? ON CONFLICT DO NOTHING",
                        ACADEMIC_DATE, MARKER + " asistencia", courseCode, studentCode);
                insert(connection,
                        "INSERT INTO calificaciones (evaluacion_id,estudiante_id,nota,observacion,actualizado_en) "
                                + "SELECT v.id,e.id,?,?,? FROM evaluaciones v "
                                + "JOIN cursos c ON c.id=v.curso_id CROSS JOIN estudiantes e "
                                + "WHERE c.codigo=? AND v.nombre=? AND e.codigo=? ON CONFLICT DO NOTHING",
                        (double) score, MARKER + " nota de prueba", SEED_TIMESTAMP, courseCode,
                        MARKER + " Evaluación " + two(course), studentCode);
            }
        }
    }

    private static void seedCompetencyResults(Connection connection) throws SQLException {
        String[] levels = {"AD", "A", "B", "C"};
        for (int student = 1; student <= STUDENT_COUNT; student++) {
            for (int course = 1; course <= COURSE_COUNT; course++) {
                insert(connection,
                        "INSERT INTO logros_competencias_blenkir "
                                + "(competencia_id,estudiante_id,periodo,nivel,conclusion) "
                                + "SELECT b.id,e.id,?,?,? FROM competencias_blenkir b "
                                + "JOIN cursos c ON c.id=b.curso_id CROSS JOIN estudiantes e "
                                + "WHERE c.codigo=? AND b.nombre=? AND e.codigo=? ON CONFLICT DO NOTHING",
                        "2026-II", levels[(student + course) % levels.length],
                        MARKER + " conclusión descriptiva de prueba.",
                        "TST26-C" + two(course), MARKER + " Competencia " + two(course),
                        "TST26-S" + four(student));
            }
        }
    }

    private static void seedFinance(Connection connection) throws SQLException {
        String enrollmentConcept = MARKER + " Matrícula";
        String monthlyConcept = MARKER + " Pensión";
        insert(connection,
                "INSERT INTO conceptos_cobro_blenkir (nombre,importe,activo) VALUES (?,?,TRUE) ON CONFLICT DO NOTHING",
                enrollmentConcept, new BigDecimal("25.00"));
        insert(connection,
                "INSERT INTO conceptos_cobro_blenkir (nombre,importe,activo) VALUES (?,?,TRUE) ON CONFLICT DO NOTHING",
                monthlyConcept, new BigDecimal("15.00"));

        for (int student = 1; student <= STUDENT_COUNT; student++) {
            String studentCode = "TST26-S" + four(student);
            insert(connection,
                    "INSERT INTO cuentas_por_cobrar "
                            + "(estudiante_id,concepto_id,importe,pagado,vencimiento,periodo) "
                            + "SELECT e.id,p.id,25.00,?,DATE '2026-11-30',? FROM estudiantes e "
                            + "JOIN conceptos_cobro_blenkir p ON p.nombre=? WHERE e.codigo=? "
                            + "ON CONFLICT DO NOTHING",
                    student <= STUDENT_COUNT / 2 ? new BigDecimal("25.00") : BigDecimal.ZERO,
                    PERIOD, enrollmentConcept, studentCode);
            insert(connection,
                    "INSERT INTO cuentas_por_cobrar "
                            + "(estudiante_id,concepto_id,importe,pagado,vencimiento,periodo) "
                            + "SELECT e.id,p.id,15.00,0,DATE '2026-12-31',? FROM estudiantes e "
                            + "JOIN conceptos_cobro_blenkir p ON p.nombre=? WHERE e.codigo=? "
                            + "ON CONFLICT DO NOTHING",
                    PERIOD, monthlyConcept, studentCode);

            if (student <= STUDENT_COUNT / 2) {
                String reference = "TST26-PAY-" + four(student);
                insertIfMissing(connection,
                        "INSERT INTO pagos_blenkir (cuenta_id,importe,medio,fecha,registrado_por,referencia) "
                                + "SELECT a.id,25.00,'EFECTIVO',?,'test-data-loader',? "
                                + "FROM cuentas_por_cobrar a JOIN estudiantes e ON e.id=a.estudiante_id "
                                + "JOIN conceptos_cobro_blenkir p ON p.id=a.concepto_id "
                                + "WHERE e.codigo=? AND p.nombre=? AND a.periodo=?",
                        "SELECT EXISTS(SELECT 1 FROM pagos_blenkir WHERE referencia=?)",
                        new Object[]{SEED_TIMESTAMP, reference, studentCode, enrollmentConcept, PERIOD},
                        new Object[]{reference});
                insert(connection,
                        "INSERT INTO movimientos_caja_blenkir (fecha,tipo,concepto,importe,usuario,pago_id) "
                                + "SELECT ?, 'INGRESO', ?,25.00,'test-data-loader',id FROM pagos_blenkir "
                                + "WHERE referencia=? ON CONFLICT DO NOTHING",
                        SEED_TIMESTAMP, MARKER + " pago de matrícula", reference);
            }
        }
    }

    private static void seedNoticesAndAudit(Connection connection) throws SQLException {
        for (int index = 1; index <= 3; index++) {
            String title = MARKER + " Comunicado " + index;
            insertIfMissing(connection,
                    "INSERT INTO comunicados_blenkir (titulo,mensaje,rol_destino,fecha,autor) "
                            + "VALUES (?,?, 'TODOS', ?, 'test-data-loader')",
                    "SELECT EXISTS(SELECT 1 FROM comunicados_blenkir WHERE titulo=?)",
                    new Object[]{title, "Mensaje ficticio de prueba " + index + ".", SEED_TIMESTAMP},
                    new Object[]{title});
            insertIfMissing(connection,
                    "INSERT INTO auditoria_blenkir (fecha,usuario,accion,entidad,referencia) "
                            + "VALUES (?,'test-data-loader','SEED','test_data',?)",
                    "SELECT EXISTS(SELECT 1 FROM auditoria_blenkir WHERE referencia=?)",
                    new Object[]{SEED_TIMESTAMP, MARKER + ":AUDIT:" + index},
                    new Object[]{MARKER + ":AUDIT:" + index});
        }
    }

    private static void clean(Connection connection) throws SQLException {
        System.out.println("Limpieza limitada a identidades y referencias con marcador " + MARKER + ".");
        delete(connection, "DELETE FROM movimientos_caja_blenkir WHERE pago_id IN "
                + "(SELECT id FROM pagos_blenkir WHERE referencia LIKE 'TST26-PAY-%')");
        delete(connection, "DELETE FROM pagos_blenkir WHERE referencia LIKE 'TST26-PAY-%'");
        delete(connection, "DELETE FROM cuentas_por_cobrar WHERE periodo=? AND estudiante_id IN "
                + "(SELECT id FROM estudiantes WHERE codigo LIKE 'TST26-S%')",
                PERIOD);
        delete(connection, "DELETE FROM logros_competencias_blenkir WHERE competencia_id IN "
                + "(SELECT id FROM competencias_blenkir WHERE nombre LIKE 'TST26 Competencia %') "
                + "OR estudiante_id IN (SELECT id FROM estudiantes WHERE codigo LIKE 'TST26-S%')");
        delete(connection, "DELETE FROM calificaciones WHERE evaluacion_id IN "
                + "(SELECT id FROM evaluaciones WHERE nombre LIKE 'TST26 Evaluación %') "
                + "OR estudiante_id IN (SELECT id FROM estudiantes WHERE codigo LIKE 'TST26-S%')");
        delete(connection, "DELETE FROM asistencias WHERE curso_id IN "
                + "(SELECT id FROM cursos WHERE codigo LIKE 'TST26-C%' "
                + "OR codigo ~ '^TST26-G[1-5]-S[A-B]-C[0-9]{2}$') "
                + "OR estudiante_id IN (SELECT id FROM estudiantes WHERE codigo LIKE 'TST26-S%')");
        delete(connection, "DELETE FROM matriculas_curso WHERE curso_id IN "
                + "(SELECT id FROM cursos WHERE codigo LIKE 'TST26-C%' "
                + "OR codigo ~ '^TST26-G[1-5]-S[A-B]-C[0-9]{2}$') "
                + "OR estudiante_id IN (SELECT id FROM estudiantes WHERE codigo LIKE 'TST26-S%')");
        delete(connection, "DELETE FROM evaluaciones WHERE nombre LIKE 'TST26 Evaluación %'");
        delete(connection, "DELETE FROM competencias_blenkir WHERE nombre LIKE 'TST26 Competencia %'");
        delete(connection, "DELETE FROM horarios_clases WHERE curso_id IN "
                + "(SELECT id FROM cursos WHERE codigo LIKE 'TST26-C%' "
                + "OR codigo ~ '^TST26-G[1-5]-S[A-B]-C[0-9]{2}$')");
        delete(connection, "DELETE FROM apoderado_estudiante WHERE estudiante_id IN "
                + "(SELECT id FROM estudiantes WHERE codigo LIKE 'TST26-S%') OR apoderado_id IN "
                + "(SELECT id FROM apoderados WHERE dni LIKE '96000%')");
        delete(connection, "DELETE FROM cursos WHERE codigo LIKE 'TST26-C%' "
                + "OR codigo ~ '^TST26-G[1-5]-S[A-B]-C[0-9]{2}$'");
        delete(connection, "DELETE FROM estudiantes WHERE codigo LIKE 'TST26-S%'");
        delete(connection, "DELETE FROM docentes WHERE codigo LIKE 'TST26-D%'");
        delete(connection, "DELETE FROM apoderados WHERE dni LIKE '96000%'");
        delete(connection, "DELETE FROM personal_colegio WHERE dni LIKE '95000%'");
        delete(connection, "DELETE FROM conceptos_cobro_blenkir WHERE nombre LIKE 'TST26 %'");
        delete(connection, "DELETE FROM comunicados_blenkir WHERE titulo LIKE 'TST26 Comunicado %'");
        delete(connection, "DELETE FROM auditoria_blenkir WHERE referencia LIKE 'TST26:%'");
        delete(connection, "DELETE FROM usuarios WHERE username LIKE 'test_seed_26_%'");
        System.out.println("Registros candidatos eliminados; transacción pendiente de confirmar.");
    }

    private static void verifySeedCounts(Connection connection) throws SQLException {
        Map<String, Long> actual = seedRowCounts(connection);

        Map<String, Integer> expected = plannedRowCounts();
        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            long found = actual.get(entry.getKey());
            if (found != entry.getValue()) {
                throw new IllegalStateException("Conteo inesperado en " + entry.getKey() + ": esperado "
                        + entry.getValue() + ", encontrado " + found + ". Se revertirá toda la carga.");
            }
        }
        System.out.println("Verificación transaccional: los 20 conteos de filas coinciden con el plan.");
    }

    private static Map<String, Long> seedRowCounts(Connection connection) throws SQLException {
        Map<String, Long> actual = new LinkedHashMap<>();
        actual.put("usuarios", count(connection,
                "SELECT COUNT(*) FROM usuarios WHERE username LIKE 'test_seed_26_%'"));
        actual.put("docentes", count(connection,
                "SELECT COUNT(*) FROM docentes WHERE codigo LIKE 'TST26-D%'"));
        actual.put("estudiantes", count(connection,
                "SELECT COUNT(*) FROM estudiantes WHERE codigo LIKE 'TST26-S%'"));
        actual.put("apoderados", count(connection,
                "SELECT COUNT(*) FROM apoderados WHERE dni LIKE '96000%'"));
        actual.put("apoderado_estudiante", count(connection,
                "SELECT COUNT(*) FROM apoderado_estudiante l JOIN estudiantes e ON e.id=l.estudiante_id "
                        + "WHERE e.codigo LIKE 'TST26-S%'"));
        actual.put("personal_colegio", count(connection,
                "SELECT COUNT(*) FROM personal_colegio WHERE dni LIKE '95000%'"));
        actual.put("cursos", count(connection,
                "SELECT COUNT(*) FROM cursos WHERE codigo LIKE 'TST26-C%'"));
        actual.put("matriculas_curso", count(connection,
                "SELECT COUNT(*) FROM matriculas_curso m JOIN estudiantes e ON e.id=m.estudiante_id "
                        + "JOIN cursos c ON c.id=m.curso_id WHERE e.codigo LIKE 'TST26-S%' "
                        + "AND c.codigo LIKE 'TST26-C%'"));
        actual.put("evaluaciones", count(connection,
                "SELECT COUNT(*) FROM evaluaciones WHERE nombre LIKE 'TST26 Evaluación %'"));
        actual.put("calificaciones", count(connection,
                "SELECT COUNT(*) FROM calificaciones g JOIN evaluaciones v ON v.id=g.evaluacion_id "
                        + "WHERE v.nombre LIKE 'TST26 Evaluación %'"));
        actual.put("asistencias", count(connection,
                "SELECT COUNT(*) FROM asistencias a JOIN cursos c ON c.id=a.curso_id "
                        + "WHERE c.codigo LIKE 'TST26-C%'"));
        actual.put("horarios_clases", count(connection,
                "SELECT COUNT(*) FROM horarios_clases h JOIN cursos c ON c.id=h.curso_id "
                        + "WHERE c.codigo LIKE 'TST26-C%'"));
        actual.put("competencias_blenkir", count(connection,
                "SELECT COUNT(*) FROM competencias_blenkir WHERE nombre LIKE 'TST26 Competencia %'"));
        actual.put("logros_competencias_blenkir", count(connection,
                "SELECT COUNT(*) FROM logros_competencias_blenkir r JOIN competencias_blenkir b "
                        + "ON b.id=r.competencia_id WHERE b.nombre LIKE 'TST26 Competencia %'"));
        actual.put("conceptos_cobro_blenkir", count(connection,
                "SELECT COUNT(*) FROM conceptos_cobro_blenkir WHERE nombre LIKE 'TST26 %'"));
        actual.put("cuentas_por_cobrar", count(connection,
                "SELECT COUNT(*) FROM cuentas_por_cobrar a JOIN estudiantes e ON e.id=a.estudiante_id "
                        + "WHERE e.codigo LIKE 'TST26-S%' AND a.periodo='TST26-2026-II'"));
        actual.put("pagos_blenkir", count(connection,
                "SELECT COUNT(*) FROM pagos_blenkir WHERE referencia LIKE 'TST26-PAY-%'"));
        actual.put("movimientos_caja_blenkir", count(connection,
                "SELECT COUNT(*) FROM movimientos_caja_blenkir m JOIN pagos_blenkir p ON p.id=m.pago_id "
                        + "WHERE p.referencia LIKE 'TST26-PAY-%'"));
        actual.put("comunicados_blenkir", count(connection,
                "SELECT COUNT(*) FROM comunicados_blenkir WHERE titulo LIKE 'TST26 Comunicado %'"));
        actual.put("auditoria_blenkir", count(connection,
                "SELECT COUNT(*) FROM auditoria_blenkir WHERE referencia LIKE 'TST26:%'"));
        return actual;
    }

    private static long count(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                throw new SQLException("La consulta de verificación no devolvió un conteo.");
            }
            return result.getLong(1);
        }
    }

    private static Map<String, Set<String>> primaryKeys(DatabaseMetaData metadata, Connection connection,
                                                         String schema) throws SQLException {
        Map<String, Set<String>> keys = new HashMap<>();
        for (String table : REQUIRED_COLUMNS.keySet()) {
            Set<String> columns = new java.util.HashSet<>();
            try (ResultSet rows = metadata.getPrimaryKeys(connection.getCatalog(), schema, table)) {
                while (rows.next()) {
                    columns.add(rows.getString("COLUMN_NAME").toLowerCase());
                }
            }
            keys.put(table, columns);
        }
        return keys;
    }

    private static Map<String, Set<Set<String>>> uniqueIndexes(DatabaseMetaData metadata, Connection connection,
                                                                String schema) throws SQLException {
        Map<String, Map<String, Map<Short, String>>> indexes = new HashMap<>();
        for (String table : REQUIRED_COLUMNS.keySet()) {
            try (ResultSet rows = metadata.getIndexInfo(connection.getCatalog(), schema, table, true, false)) {
                while (rows.next()) {
                    String indexName = rows.getString("INDEX_NAME");
                    String column = rows.getString("COLUMN_NAME");
                    if (indexName == null || column == null || "PRIMARY".equalsIgnoreCase(indexName)) {
                        continue;
                    }
                    indexes.computeIfAbsent(table, ignored -> new HashMap<>())
                            .computeIfAbsent(indexName, ignored -> new HashMap<>())
                            .put(rows.getShort("ORDINAL_POSITION"), column.toLowerCase());
                }
            }
        }
        Map<String, Set<Set<String>>> result = new HashMap<>();
        indexes.forEach((table, tableIndexes) -> {
            Set<Set<String>> uniqueKeys = new java.util.HashSet<>();
            tableIndexes.values().forEach(columns -> uniqueKeys.add(Set.copyOf(columns.values())));
            result.put(table, uniqueKeys);
        });
        return result;
    }

    private static Map<String, Set<String>> foreignKeys(DatabaseMetaData metadata, Connection connection,
                                                          String schema) throws SQLException {
        Map<String, Set<String>> keys = new HashMap<>();
        for (String table : REQUIRED_COLUMNS.keySet()) {
            Set<String> references = new java.util.HashSet<>();
            try (ResultSet rows = metadata.getImportedKeys(connection.getCatalog(), schema, table)) {
                while (rows.next()) {
                    references.add(rows.getString("FKCOLUMN_NAME").toLowerCase() + "->"
                            + rows.getString("PKTABLE_NAME").toLowerCase() + "."
                            + rows.getString("PKCOLUMN_NAME").toLowerCase());
                }
            }
            keys.put(table, references);
        }
        return keys;
    }

    private static Map<String, List<Set<String>>> expectedUniqueConstraints() {
        Map<String, List<Set<String>>> constraints = new LinkedHashMap<>();
        constraints.put("usuarios", List.of(Set.of("username")));
        constraints.put("docentes", List.of(Set.of("codigo"), Set.of("dni"), Set.of("usuario_id")));
        constraints.put("estudiantes", List.of(Set.of("codigo"), Set.of("dni"), Set.of("usuario_id")));
        constraints.put("apoderados", List.of(Set.of("dni"), Set.of("usuario_id")));
        constraints.put("apoderado_estudiante", List.of(Set.of("apoderado_id", "estudiante_id")));
        constraints.put("personal_colegio", List.of(Set.of("dni")));
        constraints.put("cursos", List.of(Set.of("codigo")));
        constraints.put("matriculas_curso", List.of(Set.of("estudiante_id", "curso_id")));
        constraints.put("calificaciones", List.of(Set.of("evaluacion_id", "estudiante_id")));
        constraints.put("asistencias", List.of(Set.of("curso_id", "estudiante_id", "fecha")));
        constraints.put("conceptos_cobro_blenkir", List.of(Set.of("nombre")));
        constraints.put("cuentas_por_cobrar", List.of(Set.of("estudiante_id", "concepto_id", "periodo")));
        constraints.put("movimientos_caja_blenkir", List.of(Set.of("pago_id")));
        constraints.put("logros_competencias_blenkir",
                List.of(Set.of("competencia_id", "estudiante_id", "periodo")));
        return constraints;
    }

    private static Map<String, List<String>> expectedForeignKeys() {
        Map<String, List<String>> constraints = new LinkedHashMap<>();
        constraints.put("docentes", List.of("usuario_id->usuarios.id"));
        constraints.put("estudiantes", List.of("usuario_id->usuarios.id"));
        constraints.put("apoderados", List.of("usuario_id->usuarios.id"));
        constraints.put("apoderado_estudiante",
                List.of("apoderado_id->apoderados.id", "estudiante_id->estudiantes.id"));
        constraints.put("cursos", List.of("docente_id->docentes.id"));
        constraints.put("matriculas_curso",
                List.of("estudiante_id->estudiantes.id", "curso_id->cursos.id"));
        constraints.put("evaluaciones", List.of("curso_id->cursos.id"));
        constraints.put("calificaciones",
                List.of("evaluacion_id->evaluaciones.id", "estudiante_id->estudiantes.id"));
        constraints.put("asistencias", List.of("curso_id->cursos.id", "estudiante_id->estudiantes.id"));
        constraints.put("horarios_clases", List.of("curso_id->cursos.id"));
        constraints.put("competencias_blenkir", List.of("curso_id->cursos.id"));
        constraints.put("logros_competencias_blenkir",
                List.of("competencia_id->competencias_blenkir.id", "estudiante_id->estudiantes.id"));
        constraints.put("cuentas_por_cobrar",
                List.of("estudiante_id->estudiantes.id", "concepto_id->conceptos_cobro_blenkir.id"));
        constraints.put("pagos_blenkir", List.of("cuenta_id->cuentas_por_cobrar.id"));
        constraints.put("movimientos_caja_blenkir", List.of("pago_id->pagos_blenkir.id"));
        return constraints;
    }

    private static int insert(Connection connection, String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            return statement.executeUpdate();
        }
    }

    private static int insertIfMissing(Connection connection, String insertSql, String existsSql,
                                       Object[] insertValues, Object[] existsValues) throws SQLException {
        if (exists(connection, existsSql, existsValues)) {
            return 0;
        }
        return insert(connection, insertSql, insertValues);
    }

    private static boolean exists(Connection connection, String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getBoolean(1);
            }
        }
    }

    private static int delete(Connection connection, String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            return statement.executeUpdate();
        }
    }

    private static void bind(PreparedStatement statement, Object[] values) throws SQLException {
        for (int index = 0; index < values.length; index++) {
            statement.setObject(index + 1, values[index]);
        }
    }

    private static String firstName(int index) {
        return FIRST_NAMES[(index - 1) % FIRST_NAMES.length];
    }

    private static String lastName(int index) {
        return LAST_NAMES[((index - 1) / FIRST_NAMES.length) % LAST_NAMES.length];
    }

    private static String fullName(int index) {
        return firstName(index) + " " + lastName(index) + " " + LAST_NAMES[(index + 7) % LAST_NAMES.length];
    }

    private static String guardianName(int index) {
        return firstName(index + 5) + " " + LAST_NAMES[(index + 3) % LAST_NAMES.length];
    }

    private static String dni(int value) {
        return String.format("%08d", value);
    }

    private static String four(int value) {
        return String.format("%04d", value);
    }

    private static String three(int value) {
        return String.format("%03d", value);
    }

    private static String two(int value) {
        return String.format("%02d", value);
    }

    private static Map<String, List<String>> requiredColumns() {
        Map<String, List<String>> tables = new LinkedHashMap<>();
        tables.put("usuarios", List.of("id", "username", "password_hash", "nombre_mostrado", "role", "activo"));
        tables.put("docentes", List.of("id", "codigo", "dni", "nombres", "apellidos", "email",
                "especialidad", "activo", "usuario_id"));
        tables.put("estudiantes", List.of("id", "codigo", "dni", "nombres", "apellidos", "fecha_nacimiento",
                "grado", "seccion", "email", "direccion", "telefono", "activo", "usuario_id"));
        tables.put("apoderados", List.of("id", "dni", "nombres", "apellidos", "telefono", "email",
                "activo", "usuario_id"));
        tables.put("apoderado_estudiante", List.of("id", "apoderado_id", "estudiante_id", "parentesco", "autorizado"));
        tables.put("personal_colegio", List.of("id", "dni", "nombre_completo", "cargo", "correo", "telefono", "activo"));
        tables.put("cursos", List.of("id", "codigo", "nombre", "grado", "seccion", "periodo", "docente_id", "activo"));
        tables.put("matriculas_curso", List.of("id", "estudiante_id", "curso_id", "activo"));
        tables.put("evaluaciones", List.of("id", "curso_id", "nombre", "tipo", "fecha", "peso"));
        tables.put("calificaciones", List.of("id", "evaluacion_id", "estudiante_id", "nota", "observacion", "actualizado_en"));
        tables.put("asistencias", List.of("id", "curso_id", "estudiante_id", "fecha", "estado", "observacion"));
        tables.put("horarios_clases", List.of("id", "curso_id", "dia_semana", "hora_inicio", "hora_fin", "aula"));
        tables.put("competencias_blenkir", List.of("id", "curso_id", "nombre", "descripcion"));
        tables.put("logros_competencias_blenkir", List.of("id", "competencia_id", "estudiante_id", "periodo", "nivel", "conclusion"));
        tables.put("conceptos_cobro_blenkir", List.of("id", "nombre", "importe", "activo"));
        tables.put("cuentas_por_cobrar", List.of("id", "estudiante_id", "concepto_id", "importe", "pagado",
                "vencimiento", "periodo", "version"));
        tables.put("pagos_blenkir", List.of("id", "cuenta_id", "importe", "medio", "fecha", "registrado_por", "referencia"));
        tables.put("movimientos_caja_blenkir", List.of("id", "fecha", "tipo", "concepto", "importe", "usuario", "pago_id"));
        tables.put("comunicados_blenkir", List.of("id", "titulo", "mensaje", "rol_destino", "fecha", "autor"));
        tables.put("auditoria_blenkir", List.of("id", "fecha", "usuario", "accion", "entidad", "referencia"));
        return Map.copyOf(tables);
    }
}
