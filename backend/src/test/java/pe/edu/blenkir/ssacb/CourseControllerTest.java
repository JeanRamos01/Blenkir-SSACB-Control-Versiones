package pe.edu.blenkir.ssacb;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CourseControllerTest {
    private final CourseRepository courses = mock(CourseRepository.class);
    private final TeacherRepository teachers = mock(TeacherRepository.class);
    private final EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
    private final StudentRepository students = mock(StudentRepository.class);
    private final CourseController controller = new CourseController(courses, teachers, enrollments, students);

    @Test
    void rejectsEnrollmentWhenGradeOrSectionDoesNotMatch() {
        when(courses.findById(1L)).thenReturn(Optional.of(Course.builder()
                .id(1L).grado("Primero de secundaria").seccion("A").build()));
        when(students.findById(2L)).thenReturn(Optional.of(Student.builder()
                .id(2L).grado("Primero de secundaria").seccion("B").build()));

        assertThrows(IllegalArgumentException.class, () -> controller.enroll(1L, 2L));
        verify(enrollments, never()).save(any());
    }

    @Test
    void rejectsEnrollmentWhenStudentOrCourseIsInactive() {
        when(courses.findById(1L)).thenReturn(Optional.of(Course.builder()
                .id(1L).grado("Primero de secundaria").seccion("A").activo(false).build()));
        when(students.findById(2L)).thenReturn(Optional.of(Student.builder()
                .id(2L).grado("Primero de secundaria").seccion("A").build()));

        assertThrows(IllegalArgumentException.class, () -> controller.enroll(1L, 2L));
        verify(enrollments, never()).save(any());
    }

    @Test
    void rejectsEnrollmentWhenStudentIsInactive() {
        when(courses.findById(1L)).thenReturn(Optional.of(Course.builder()
                .id(1L).grado("Primero de secundaria").seccion("A").build()));
        when(students.findById(2L)).thenReturn(Optional.of(Student.builder()
                .id(2L).grado("Primero de secundaria").seccion("A").activo(false).build()));

        assertThrows(IllegalArgumentException.class, () -> controller.enroll(1L, 2L));
        verify(enrollments, never()).save(any());
    }
}
