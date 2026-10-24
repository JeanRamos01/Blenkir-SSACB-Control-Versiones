package pe.edu.blenkir.ssacb;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DniUniquenessService {
    private final StudentRepository students;
    private final TeacherRepository teachers;
    private final GuardianRepository guardians;

    public DniUniquenessService(StudentRepository students, TeacherRepository teachers, GuardianRepository guardians) {
        this.students = students;
        this.teachers = teachers;
        this.guardians = guardians;
    }

    public String validateAndEnsureAvailable(String dni) {
        return validateAndEnsureAvailable(dni, null, null, null);
    }

    public String validateAndEnsureAvailable(String dni, Long ignoreStudentId, Long ignoreTeacherId, Long ignoreGuardianId) {
        String normalizedDni = DniValidator.requireValid(dni);

        if (students.findByDni(normalizedDni)
                .filter(student -> !Objects.equals(student.getId(), ignoreStudentId))
                .isPresent()) {
            throw new IllegalArgumentException("El DNI ya está registrado por un estudiante.");
        }

        if (teachers.findByDni(normalizedDni)
                .filter(teacher -> !Objects.equals(teacher.getId(), ignoreTeacherId))
                .isPresent()) {
            throw new IllegalArgumentException("El DNI ya está registrado por un docente.");
        }

        if (guardians.findByDni(normalizedDni)
                .filter(guardian -> !Objects.equals(guardian.getId(), ignoreGuardianId))
                .isPresent()) {
            throw new IllegalArgumentException("El DNI ya está registrado por un apoderado.");
        }

        return normalizedDni;
    }
}
