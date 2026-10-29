\set ON_ERROR_STOP on
BEGIN READ ONLY;

SELECT current_database() AS database_name, current_user AS database_user,
       current_setting('server_version') AS postgres_version;

SELECT 'estudiantes TST26' AS conjunto, count(*) AS filas
FROM estudiantes
WHERE codigo LIKE 'TST26-S%'
UNION ALL
SELECT 'docentes TST26', count(*)
FROM docentes
WHERE codigo LIKE 'TST26-D%'
UNION ALL
SELECT 'cursos TST26', count(*)
FROM cursos
WHERE codigo LIKE 'TST26-%'
UNION ALL
SELECT 'matriculas de estudiantes TST26', count(*)
FROM matriculas_curso m
JOIN estudiantes s ON s.id = m.estudiante_id
WHERE s.codigo LIKE 'TST26-S%'
UNION ALL
SELECT 'calificaciones de estudiantes TST26', count(*)
FROM calificaciones g
JOIN estudiantes s ON s.id = g.estudiante_id
WHERE s.codigo LIKE 'TST26-S%'
UNION ALL
SELECT 'asistencias de estudiantes TST26', count(*)
FROM asistencias a
JOIN estudiantes s ON s.id = a.estudiante_id
WHERE s.codigo LIKE 'TST26-S%'
UNION ALL
SELECT 'evaluaciones de cursos TST26', count(*)
FROM evaluaciones e
JOIN cursos c ON c.id = e.curso_id
WHERE c.codigo LIKE 'TST26-%'
UNION ALL
SELECT 'horarios de cursos TST26', count(*)
FROM horarios_clases h
JOIN cursos c ON c.id = h.curso_id
WHERE c.codigo LIKE 'TST26-%'
UNION ALL
SELECT 'competencias de cursos TST26', count(*)
FROM competencias_blenkir b
JOIN cursos c ON c.id = b.curso_id
WHERE c.codigo LIKE 'TST26-%'
UNION ALL
SELECT 'logros de estudiantes TST26', count(*)
FROM logros_competencias_blenkir r
JOIN estudiantes s ON s.id = r.estudiante_id
WHERE s.codigo LIKE 'TST26-S%'
ORDER BY conjunto;

SELECT grado, seccion, count(*) AS estudiantes
FROM estudiantes
WHERE codigo LIKE 'TST26-S%'
GROUP BY grado, seccion
ORDER BY grado, seccion;

SELECT c.grado, c.seccion, c.periodo, count(*) AS cursos
FROM cursos c
WHERE c.codigo LIKE 'TST26-%'
GROUP BY c.grado, c.seccion, c.periodo
ORDER BY c.grado, c.seccion, c.periodo;

WITH cohorts AS (
    SELECT
        series.group_no,
        (series.group_no / 2) + 1 AS grade_no,
        CASE WHEN series.group_no % 2 = 0 THEN 'A' ELSE 'B' END AS section,
        CASE (series.group_no / 2) + 1
            WHEN 1 THEN '1ro Secundaria'
            WHEN 2 THEN '2do Secundaria'
            WHEN 3 THEN '3ro Secundaria'
            WHEN 4 THEN '4to Secundaria'
            WHEN 5 THEN '5to Secundaria'
        END AS grade_label
    FROM generate_series(0, 9) AS series(group_no)
),
subjects AS (
    SELECT
        c.nombre,
        substring(c.codigo FROM '^TST26-C([0-9]{2})$')::integer AS subject_no
    FROM cursos c
    WHERE c.codigo ~ '^TST26-C[0-9]{2}$'
),
targets AS (
    SELECT
        g.*,
        s.nombre,
        CASE
            WHEN g.group_no = 8
                THEN 'TST26-C' || lpad(s.subject_no::text, 2, '0')
            ELSE 'TST26-G' || g.grade_no::text || '-S' || g.section
                 || '-C' || lpad(s.subject_no::text, 2, '0')
        END AS target_code
    FROM cohorts g
    CROSS JOIN subjects s
)
SELECT
    (SELECT count(*) FROM targets) AS target_course_rows,
    (SELECT count(*) FROM targets t
     JOIN cursos c ON c.codigo = t.target_code) AS existing_target_courses,
    (SELECT count(*) FROM targets t
     LEFT JOIN cursos c ON c.codigo = t.target_code
     WHERE c.id IS NULL) AS courses_to_insert,
    (SELECT count(*) FROM targets t
     JOIN cursos c
       ON lower(regexp_replace(trim(c.nombre), '\s+', ' ', 'g'))
          = lower(regexp_replace(trim(t.nombre), '\s+', ' ', 'g'))
      AND c.grado = t.grade_label
      AND c.seccion = t.section
      AND c.periodo = '2026-II'
      AND c.codigo <> t.target_code) AS non_test_course_conflicts;

WITH cohorts AS (
    SELECT
        series.group_no,
        (series.group_no / 2) + 1 AS grade_no,
        CASE WHEN series.group_no % 2 = 0 THEN 'A' ELSE 'B' END AS section,
        CASE (series.group_no / 2) + 1
            WHEN 1 THEN '1ro Secundaria'
            WHEN 2 THEN '2do Secundaria'
            WHEN 3 THEN '3ro Secundaria'
            WHEN 4 THEN '4to Secundaria'
            WHEN 5 THEN '5to Secundaria'
        END AS grade_label
    FROM generate_series(0, 9) AS series(group_no)
)
SELECT
    cohort.grade_label AS grado,
    cohort.section AS seccion,
    (SELECT count(*) FROM estudiantes s
     WHERE s.codigo LIKE 'TST26-S%'
       AND s.grado = cohort.grade_label AND s.seccion = cohort.section) AS estudiantes,
    (SELECT count(*) FROM cursos c
     WHERE c.codigo LIKE 'TST26-%'
       AND c.grado = cohort.grade_label AND c.seccion = cohort.section
       AND c.periodo = '2026-II') AS cursos,
    (SELECT count(*) FROM matriculas_curso m
     JOIN estudiantes s ON s.id = m.estudiante_id
     JOIN cursos c ON c.id = m.curso_id
     WHERE s.codigo LIKE 'TST26-S%'
       AND s.grado = cohort.grade_label AND s.seccion = cohort.section
       AND c.codigo LIKE 'TST26-%' AND m.activo) AS matriculas_activas,
    (SELECT count(*) FROM calificaciones g
     JOIN estudiantes s ON s.id = g.estudiante_id
     WHERE s.codigo LIKE 'TST26-S%'
       AND s.grado = cohort.grade_label AND s.seccion = cohort.section) AS calificaciones,
    (SELECT count(*) FROM asistencias a
     JOIN estudiantes s ON s.id = a.estudiante_id
     WHERE s.codigo LIKE 'TST26-S%'
       AND s.grado = cohort.grade_label AND s.seccion = cohort.section) AS asistencias,
    (SELECT count(*) FROM logros_competencias_blenkir r
     JOIN estudiantes s ON s.id = r.estudiante_id
     WHERE s.codigo LIKE 'TST26-S%'
       AND s.grado = cohort.grade_label AND s.seccion = cohort.section) AS logros_competencias,
    (SELECT count(*) FROM horarios_clases h
     JOIN cursos c ON c.id = h.curso_id
     WHERE c.codigo LIKE 'TST26-%'
       AND c.grado = cohort.grade_label AND c.seccion = cohort.section
       AND c.periodo = '2026-II') AS horarios
FROM cohorts cohort
ORDER BY cohort.grade_no, cohort.section;

COMMIT;
