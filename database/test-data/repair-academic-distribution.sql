\if :{?allow_write}
\if :allow_write
\else
\echo 'Refusing to run without -v allow_write=on'
\quit 3
\endif
\else
\echo 'Refusing to run without -v allow_write=on'
\quit 3
\endif

\set ON_ERROR_STOP on
BEGIN;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
SELECT pg_advisory_xact_lock(26061029, 2);

CREATE TEMP TABLE td_students ON COMMIT DROP AS
SELECT
    s.id,
    s.codigo,
    substring(s.codigo FROM 'TST26-S([0-9]{4})$')::integer AS student_no,
    (substring(s.codigo FROM 'TST26-S([0-9]{4})$')::integer - 1) / 20 AS group_no,
    ((substring(s.codigo FROM 'TST26-S([0-9]{4})$')::integer - 1) / 40) + 1 AS grade_no,
    CASE ((substring(s.codigo FROM 'TST26-S([0-9]{4})$')::integer - 1) / 20) % 2
        WHEN 0 THEN 'A'
        ELSE 'B'
    END AS section,
    CASE ((substring(s.codigo FROM 'TST26-S([0-9]{4})$')::integer - 1) / 40) + 1
        WHEN 1 THEN '1ro Secundaria'
        WHEN 2 THEN '2do Secundaria'
        WHEN 3 THEN '3ro Secundaria'
        WHEN 4 THEN '4to Secundaria'
        WHEN 5 THEN '5to Secundaria'
    END AS grade_label
FROM estudiantes s
WHERE s.codigo ~ '^TST26-S[0-9]{4}$';

CREATE TEMP TABLE td_subjects ON COMMIT DROP AS
SELECT
    c.id AS legacy_course_id,
    substring(c.codigo FROM '^TST26-C([0-9]{2})$')::integer AS subject_no,
    c.nombre
FROM cursos c
WHERE c.codigo ~ '^TST26-C[0-9]{2}$';

DO $$
DECLARE
    test_student_count integer;
    test_teacher_count integer;
    legacy_course_count integer;
    extra_course_count integer;
    test_enrollment_count integer;
    test_evaluation_count integer;
    test_grade_count integer;
    test_attendance_count integer;
    test_schedule_count integer;
    test_competency_count integer;
    test_result_count integer;
BEGIN
    SELECT count(*) INTO test_student_count FROM td_students;
    SELECT count(*) INTO test_teacher_count FROM docentes WHERE codigo LIKE 'TST26-D%';
    SELECT count(*) INTO legacy_course_count FROM td_subjects;
    SELECT count(*) INTO extra_course_count
    FROM cursos
    WHERE codigo ~ '^TST26-G[1-5]-S[A-B]-C[0-9]{2}$';
    SELECT count(*) INTO test_enrollment_count
    FROM matriculas_curso m JOIN td_students s ON s.id = m.estudiante_id;
    SELECT count(*) INTO test_evaluation_count
    FROM evaluaciones e JOIN cursos c ON c.id = e.curso_id
    WHERE c.codigo LIKE 'TST26-%';
    SELECT count(*) INTO test_grade_count
    FROM calificaciones g JOIN td_students s ON s.id = g.estudiante_id;
    SELECT count(*) INTO test_attendance_count
    FROM asistencias a JOIN td_students s ON s.id = a.estudiante_id;
    SELECT count(*) INTO test_schedule_count
    FROM horarios_clases h JOIN cursos c ON c.id = h.curso_id
    WHERE c.codigo LIKE 'TST26-%';
    SELECT count(*) INTO test_competency_count
    FROM competencias_blenkir b JOIN cursos c ON c.id = b.curso_id
    WHERE c.codigo LIKE 'TST26-%';
    SELECT count(*) INTO test_result_count
    FROM logros_competencias_blenkir r JOIN td_students s ON s.id = r.estudiante_id;

    IF test_student_count <> 200 OR test_teacher_count <> 20 OR legacy_course_count <> 13 THEN
        RAISE EXCEPTION 'Unexpected test fixture: students %, teachers %, legacy courses %; expected 200, 20, 13.',
            test_student_count, test_teacher_count, legacy_course_count;
    END IF;
    IF extra_course_count NOT IN (0, 117) THEN
        RAISE EXCEPTION 'Found % additional marked courses; expected 0 before repair or 117 after repair.',
            extra_course_count;
    END IF;
    IF test_enrollment_count <> 2600 OR test_grade_count <> 2600
       OR test_attendance_count <> 2600 OR test_result_count <> 2600 THEN
        RAISE EXCEPTION 'Unexpected test relationship counts: enrollments %, grades %, attendance %, competency results %.',
            test_enrollment_count, test_grade_count, test_attendance_count, test_result_count;
    END IF;
    IF test_evaluation_count NOT IN (13, 130)
       OR test_schedule_count NOT IN (13, 130)
       OR test_competency_count NOT IN (13, 130) THEN
        RAISE EXCEPTION 'Unexpected test evaluation/schedule/competency counts: %, %, %.',
            test_evaluation_count, test_schedule_count, test_competency_count;
    END IF;
    IF (SELECT count(DISTINCT student_no) FROM td_students) <> 200
       OR (SELECT min(student_no) FROM td_students) <> 1
       OR (SELECT max(student_no) FROM td_students) <> 200 THEN
        RAISE EXCEPTION 'Test student codes must cover the unique range 0001 through 0200.';
    END IF;
    IF (SELECT count(DISTINCT subject_no) FROM td_subjects) <> 13
       OR (SELECT min(subject_no) FROM td_subjects) <> 1
       OR (SELECT max(subject_no) FROM td_subjects) <> 13 THEN
        RAISE EXCEPTION 'Legacy test course codes must cover the unique range 01 through 13.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM td_subjects s
        WHERE (SELECT count(*) FROM docentes t
               WHERE t.codigo LIKE 'TST26-D%'
                 AND t.especialidad = s.nombre) NOT IN (1, 2)
    ) THEN
        RAISE EXCEPTION 'Each marked subject must have one or two marked specialist teachers.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM horarios_clases h
        JOIN cursos c ON c.id = h.curso_id
        WHERE c.codigo LIKE 'TST26-%'
        GROUP BY h.curso_id
        HAVING count(*) <> 1
    ) THEN
        RAISE EXCEPTION 'Each marked course must have exactly one schedule before repair.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM evaluaciones e
        JOIN cursos c ON c.id = e.curso_id
        WHERE c.codigo LIKE 'TST26-%'
        GROUP BY e.curso_id
        HAVING count(*) <> 1
    ) THEN
        RAISE EXCEPTION 'Each marked course must have exactly one evaluation before repair.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM competencias_blenkir b
        JOIN cursos c ON c.id = b.curso_id
        WHERE c.codigo LIKE 'TST26-%'
        GROUP BY b.curso_id
        HAVING count(*) <> 1
    ) THEN
        RAISE EXCEPTION 'Each marked course must have exactly one competency before repair.';
    END IF;
END
$$;

CREATE TEMP TABLE td_targets ON COMMIT DROP AS
WITH cohorts AS (
    SELECT
        n AS group_no,
        (n / 2) + 1 AS grade_no,
        CASE WHEN n % 2 = 0 THEN 'A' ELSE 'B' END AS section,
        CASE (n / 2) + 1
            WHEN 1 THEN '1ro Secundaria'
            WHEN 2 THEN '2do Secundaria'
            WHEN 3 THEN '3ro Secundaria'
            WHEN 4 THEN '4to Secundaria'
            WHEN 5 THEN '5to Secundaria'
        END AS grade_label
    FROM generate_series(0, 9) AS series(n)
),
other_group_order AS (
    SELECT
        g.group_no,
        s.subject_no,
        CASE WHEN g.group_no < 8 THEN g.group_no + 1 ELSE 9 END AS group_rank
    FROM cohorts g
    CROSS JOIN td_subjects s
    WHERE g.group_no <> 8
)
SELECT
    g.group_no,
    g.grade_no,
    g.section,
    g.grade_label,
    s.subject_no,
    s.nombre AS subject_name,
    CASE
        WHEN g.group_no = 8 THEN s.subject_no
        WHEN s.subject_no > 7 THEN s.subject_no
        WHEN o.group_rank % 2 = 0 THEN s.subject_no
        ELSE s.subject_no + 13
    END AS teacher_no,
    CASE
        WHEN g.group_no = 8 THEN 'TST26-C' || lpad(s.subject_no::text, 2, '0')
        ELSE 'TST26-G' || g.grade_no::text || '-S' || g.section
             || '-C' || lpad(s.subject_no::text, 2, '0')
    END AS course_code,
    ((s.subject_no - 1 + g.group_no) % 13) AS slot_no
FROM cohorts g
CROSS JOIN td_subjects s
LEFT JOIN other_group_order o ON o.group_no = g.group_no AND o.subject_no = s.subject_no;

DO $$
BEGIN
    IF (SELECT count(*) FROM td_targets) <> 130
       OR (SELECT count(DISTINCT course_code) FROM td_targets) <> 130 THEN
        RAISE EXCEPTION 'The target curriculum must contain 130 unique courses.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM td_targets x
        LEFT JOIN docentes t ON t.codigo = 'TST26-D' || lpad(x.teacher_no::text, 3, '0')
        WHERE t.id IS NULL OR t.especialidad IS DISTINCT FROM x.subject_name
    ) THEN
        RAISE EXCEPTION 'A target course has no marked teacher with matching specialty.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM td_targets target
        JOIN cursos existing
          ON lower(regexp_replace(trim(existing.nombre), '\s+', ' ', 'g'))
             = lower(regexp_replace(trim(target.subject_name), '\s+', ' ', 'g'))
         AND existing.grado = target.grade_label
         AND existing.seccion = target.section
         AND existing.periodo = '2026-II'
         AND existing.codigo <> target.course_code
    ) THEN
        RAISE EXCEPTION 'An existing course conflicts with a target subject, grade, section, and period.';
    END IF;
END
$$;

UPDATE estudiantes s
SET grado = t.grade_label, seccion = t.section
FROM td_students t
WHERE s.id = t.id;

INSERT INTO cursos (codigo, nombre, grado, seccion, periodo, docente_id, activo)
SELECT
    t.course_code,
    t.subject_name,
    t.grade_label,
    t.section,
    '2026-II',
    d.id,
    TRUE
FROM td_targets t
JOIN docentes d ON d.codigo = 'TST26-D' || lpad(t.teacher_no::text, 3, '0')
WHERE t.group_no <> 8
ON CONFLICT (codigo) DO NOTHING;

CREATE TEMP TABLE td_courses ON COMMIT DROP AS
SELECT
    t.*,
    c.id AS course_id,
    d.id AS teacher_id
FROM td_targets t
JOIN cursos c ON c.codigo = t.course_code
JOIN docentes d ON d.codigo = 'TST26-D' || lpad(t.teacher_no::text, 3, '0');

DO $$
BEGIN
    IF (SELECT count(*) FROM td_courses) <> 130 THEN
        RAISE EXCEPTION 'Expected 130 mapped courses; found %.', (SELECT count(*) FROM td_courses);
    END IF;
END
$$;

UPDATE cursos c
SET
    nombre = t.subject_name,
    grado = t.grade_label,
    seccion = t.section,
    periodo = '2026-II',
    docente_id = t.teacher_id,
    activo = TRUE
FROM td_courses t
WHERE c.id = t.course_id;

INSERT INTO evaluaciones (curso_id, nombre, tipo, fecha, peso)
SELECT
    t.course_id,
    CASE
        WHEN t.group_no = 8 THEN U&'TST26 Evaluaci\00f3n ' || lpad(t.subject_no::text, 2, '0')
        ELSE U&'TST26 Evaluaci\00f3n ' || lpad(t.subject_no::text, 2, '0')
             || ' G' || t.grade_no::text || t.section
    END,
    'Prueba',
    DATE '2026-10-15',
    100
FROM td_courses t
WHERE NOT EXISTS (
    SELECT 1
    FROM evaluaciones e
    WHERE e.curso_id = t.course_id
);

CREATE TEMP TABLE td_evaluations ON COMMIT DROP AS
SELECT t.*, e.id AS evaluation_id
FROM td_courses t
JOIN evaluaciones e
  ON e.curso_id = t.course_id
 AND e.nombre = CASE
        WHEN t.group_no = 8 THEN U&'TST26 Evaluaci\00f3n ' || lpad(t.subject_no::text, 2, '0')
        ELSE U&'TST26 Evaluaci\00f3n ' || lpad(t.subject_no::text, 2, '0')
             || ' G' || t.grade_no::text || t.section
    END;

INSERT INTO competencias_blenkir (curso_id, nombre, descripcion)
SELECT
    t.course_id,
    CASE
        WHEN t.group_no = 8 THEN 'TST26 Competencia ' || lpad(t.subject_no::text, 2, '0')
        ELSE 'TST26 Competencia ' || lpad(t.subject_no::text, 2, '0')
             || ' G' || t.grade_no::text || t.section
    END,
    'Competencia sintetica de prueba.'
FROM td_courses t
WHERE NOT EXISTS (
    SELECT 1
    FROM competencias_blenkir b
    WHERE b.curso_id = t.course_id
);

CREATE TEMP TABLE td_competencies ON COMMIT DROP AS
SELECT t.*, b.id AS competency_id
FROM td_courses t
JOIN competencias_blenkir b
  ON b.curso_id = t.course_id
 AND b.nombre = CASE
        WHEN t.group_no = 8 THEN 'TST26 Competencia ' || lpad(t.subject_no::text, 2, '0')
        ELSE 'TST26 Competencia ' || lpad(t.subject_no::text, 2, '0')
             || ' G' || t.grade_no::text || t.section
    END;

DO $$
BEGIN
    IF (SELECT count(*) FROM td_evaluations) <> 130
       OR (SELECT count(DISTINCT evaluation_id) FROM td_evaluations) <> 130 THEN
        RAISE EXCEPTION 'Expected exactly one evaluation for each of the 130 courses.';
    END IF;
    IF (SELECT count(*) FROM td_competencies) <> 130
       OR (SELECT count(DISTINCT competency_id) FROM td_competencies) <> 130 THEN
        RAISE EXCEPTION 'Expected exactly one competency for each of the 130 courses.';
    END IF;
END
$$;

UPDATE horarios_clases h
SET
    dia_semana = (t.slot_no % 5) + 1,
    hora_inicio = make_time(8 + (t.slot_no / 5) * 2, 0, 0),
    hora_fin = make_time(8 + (t.slot_no / 5) * 2, 0, 0) + INTERVAL '1 hour',
    aula = 'Aula ' || t.grade_no::text || '-' || t.section
FROM td_courses t
WHERE h.curso_id = t.course_id;

INSERT INTO horarios_clases (curso_id, dia_semana, hora_inicio, hora_fin, aula)
SELECT
    t.course_id,
    (t.slot_no % 5) + 1,
    make_time(8 + (t.slot_no / 5) * 2, 0, 0),
    make_time(8 + (t.slot_no / 5) * 2, 0, 0) + INTERVAL '1 hour',
    'Aula ' || t.grade_no::text || '-' || t.section
FROM td_courses t
WHERE NOT EXISTS (
    SELECT 1 FROM horarios_clases h WHERE h.curso_id = t.course_id
);

UPDATE matriculas_curso m
SET curso_id = target.course_id
FROM td_students s, td_courses old_course, td_courses target
WHERE m.estudiante_id = s.id
  AND old_course.course_id = m.curso_id
  AND target.group_no = s.group_no
  AND target.subject_no = old_course.subject_no;

UPDATE calificaciones g
SET evaluacion_id = target.evaluation_id
FROM td_students s, td_evaluations old_evaluation, td_evaluations target
WHERE g.estudiante_id = s.id
  AND old_evaluation.evaluation_id = g.evaluacion_id
  AND target.group_no = s.group_no
  AND target.subject_no = old_evaluation.subject_no;

UPDATE asistencias a
SET
    curso_id = target.course_id,
    fecha = DATE '2026-10-05'
        + ((target.slot_no % 5))
        + (7 * ((s.student_no + target.subject_no) % 2)),
    estado = CASE (s.student_no * 7 + target.subject_no * 3) % 10
        WHEN 7 THEN 'AUSENTE'
        WHEN 8 THEN 'TARDANZA'
        WHEN 9 THEN 'JUSTIFICADO'
        ELSE 'PRESENTE'
    END,
    observacion = CASE (s.student_no * 7 + target.subject_no * 3) % 10
        WHEN 7 THEN 'TST26 ausencia de prueba.'
        WHEN 8 THEN 'TST26 tardanza de prueba.'
        WHEN 9 THEN 'TST26 justificacion ficticia.'
        ELSE 'TST26 asistencia de prueba.'
    END
FROM td_students s, td_courses old_course, td_courses target
WHERE a.estudiante_id = s.id
  AND old_course.course_id = a.curso_id
  AND target.group_no = s.group_no
  AND target.subject_no = old_course.subject_no;

UPDATE logros_competencias_blenkir r
SET competencia_id = target.competency_id
FROM td_students s, td_competencies old_competency, td_competencies target
WHERE r.estudiante_id = s.id
  AND old_competency.competency_id = r.competencia_id
  AND target.group_no = s.group_no
  AND target.subject_no = old_competency.subject_no;

UPDATE calificaciones g
SET
    nota = 8 + ((s.student_no * 7 + target.subject_no * 3) % 13),
    observacion = 'TST26 resultado sintetico de prueba.',
    actualizado_en = TIMESTAMP '2026-10-15 12:00:00'
FROM td_students s
JOIN td_evaluations target ON target.group_no = s.group_no
WHERE g.estudiante_id = s.id
  AND g.evaluacion_id = target.evaluation_id;

DO $$
DECLARE
    test_grade_count integer;
    test_attendance_count integer;
    test_enrollment_count integer;
    test_result_count integer;
    test_course_count integer;
BEGIN
    SELECT count(*) INTO test_course_count FROM td_courses;
    SELECT count(*) INTO test_enrollment_count
    FROM matriculas_curso m JOIN td_students s ON s.id = m.estudiante_id
    JOIN td_courses c ON c.course_id = m.curso_id;
    SELECT count(*) INTO test_grade_count
    FROM calificaciones g JOIN td_students s ON s.id = g.estudiante_id
    JOIN td_evaluations e ON e.evaluation_id = g.evaluacion_id;
    SELECT count(*) INTO test_attendance_count
    FROM asistencias a JOIN td_students s ON s.id = a.estudiante_id
    JOIN td_courses c ON c.course_id = a.curso_id;
    SELECT count(*) INTO test_result_count
    FROM logros_competencias_blenkir r JOIN td_students s ON s.id = r.estudiante_id
    JOIN td_competencies b ON b.competency_id = r.competencia_id;

    IF test_course_count <> 130 OR test_enrollment_count <> 2600
       OR test_grade_count <> 2600 OR test_attendance_count <> 2600
       OR test_result_count <> 2600 THEN
        RAISE EXCEPTION 'Post-repair relation counts are inconsistent: courses %, enrollments %, grades %, attendance %, competency results %.',
            test_course_count, test_enrollment_count, test_grade_count, test_attendance_count, test_result_count;
    END IF;
    IF EXISTS (
        SELECT 1
        FROM matriculas_curso m
        JOIN td_students s ON s.id = m.estudiante_id
        JOIN td_courses c ON c.course_id = m.curso_id
        WHERE s.grade_label IS DISTINCT FROM c.grade_label
           OR s.section IS DISTINCT FROM c.section
    ) THEN
        RAISE EXCEPTION 'A marked enrollment does not match the student grade and section.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM td_students s
        LEFT JOIN matriculas_curso m ON m.estudiante_id = s.id AND m.activo
        GROUP BY s.id
        HAVING count(m.id) <> 13
    ) THEN
        RAISE EXCEPTION 'Each marked student must have 13 active subject enrollments.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM td_students s
        JOIN calificaciones g ON g.estudiante_id = s.id
        WHERE g.nota < 0 OR g.nota > 20
    ) THEN
        RAISE EXCEPTION 'A marked grade is outside the 0-20 scale.';
    END IF;
    IF (SELECT count(*) FROM td_students s
        JOIN calificaciones g ON g.estudiante_id = s.id WHERE g.nota < 11) = 0
       OR (SELECT count(*) FROM td_students s
           JOIN calificaciones g ON g.estudiante_id = s.id WHERE g.nota >= 11) = 0 THEN
        RAISE EXCEPTION 'The marked grade distribution must contain both passing and failing results.';
    END IF;
    IF (SELECT count(DISTINCT estado) FROM asistencias a
        JOIN td_students s ON s.id = a.estudiante_id) <> 4 THEN
        RAISE EXCEPTION 'The marked attendance distribution must include all four supported states.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM td_courses c
        JOIN docentes d ON d.id = c.teacher_id
        WHERE d.especialidad IS DISTINCT FROM c.subject_name
    ) THEN
        RAISE EXCEPTION 'A marked course is assigned to a teacher outside the subject specialty.';
    END IF;
    IF (SELECT count(DISTINCT teacher_id) FROM td_courses) <> 20 THEN
        RAISE EXCEPTION 'All 20 marked teachers must receive an assignment.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM horarios_clases a
        JOIN td_courses ca ON ca.course_id = a.curso_id
        JOIN horarios_clases b
          ON b.id > a.id
         AND b.dia_semana = a.dia_semana
         AND a.hora_inicio < b.hora_fin
         AND b.hora_inicio < a.hora_fin
        JOIN td_courses cb ON cb.course_id = b.curso_id
        WHERE ca.teacher_id = cb.teacher_id
           OR (ca.grade_label = cb.grade_label AND ca.section = cb.section)
           OR a.aula = b.aula
    ) THEN
        RAISE EXCEPTION 'The marked schedule contains a teacher, section, or classroom conflict.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM td_courses c
        JOIN cursos course ON course.id = c.course_id
        GROUP BY lower(regexp_replace(trim(course.nombre), '\s+', ' ', 'g')),
                 course.grado, course.seccion, course.periodo
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'A duplicate marked subject exists in the same grade, section, and period.';
    END IF;
END
$$;

COMMIT;
