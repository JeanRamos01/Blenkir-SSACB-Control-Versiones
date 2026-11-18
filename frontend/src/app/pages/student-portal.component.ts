import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../core/api.service';

type StudentSection = 'profile' | 'courses' | 'grades' | 'attendance';

interface StudentProfile {
  codigo: string;
  dni: string;
  nombres: string;
  apellidos: string;
  fechaNacimiento: string | null;
  grado: string;
  seccion: string;
  email: string | null;
  direccion: string | null;
  telefono: string | null;
  activo: boolean;
}

interface StudentCourse {
  codigo: string;
  nombre: string;
  grado: string;
  seccion: string;
  periodo: string;
  docente: string | null;
}

interface StudentGrade {
  curso: string;
  evaluacion: string;
  fecha: string;
  nota: number;
  observacion: string | null;
}

interface StudentAttendance {
  fecha: string;
  curso: string;
  estado: string;
  observacion: string | null;
}

interface StudentSummary {
  student: StudentProfile;
  courses: StudentCourse[];
  grades: StudentGrade[];
  attendance: StudentAttendance[];
}

@Component({
  selector: 'app-student-portal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <ng-container *ngIf="data; else loading">
      <div class="page-head">
        <div>
          <h2>{{ pageTitle }}</h2>
          <p>{{ pageDescription }}</p>
        </div>
        <span class="status" [class.ok]="data.student.activo" [class.warn]="!data.student.activo">
          {{ data.student.activo ? 'Estudiante activo' : 'Estudiante inactivo' }}
        </span>
      </div>

      <ng-container [ngSwitch]="section">
        <section *ngSwitchCase="'profile'" class="panel">
          <h3>Información personal</h3>
          <div class="student-profile-grid">
            <div><small>Código</small><strong>{{ data.student.codigo }}</strong></div>
            <div><small>DNI</small><strong>{{ data.student.dni }}</strong></div>
            <div><small>Nombres</small><strong>{{ data.student.nombres }}</strong></div>
            <div><small>Apellidos</small><strong>{{ data.student.apellidos }}</strong></div>
            <div><small>Fecha de nacimiento</small><strong>{{ data.student.fechaNacimiento || 'No registrada' }}</strong></div>
            <div><small>Grado y sección</small><strong>{{ data.student.grado }} · {{ data.student.seccion }}</strong></div>
            <div><small>Correo electrónico</small><strong>{{ data.student.email || 'No registrado' }}</strong></div>
            <div><small>Teléfono</small><strong>{{ data.student.telefono || 'No registrado' }}</strong></div>
            <div class="profile-address"><small>Dirección</small><strong>{{ data.student.direccion || 'No registrada' }}</strong></div>
          </div>
        </section>

        <section *ngSwitchCase="'courses'" class="panel">
          <h3>Mis cursos ({{ data.courses.length }})</h3>
          <div class="table-responsive">
            <table class="table table-hover">
              <thead><tr><th>Código</th><th>Curso</th><th>Grado / Sección</th><th>Periodo</th><th>Docente</th></tr></thead>
              <tbody>
                <tr *ngFor="let course of data.courses">
                  <td>{{ course.codigo }}</td>
                  <td class="fw-semibold">{{ course.nombre }}</td>
                  <td>{{ course.grado }} · {{ course.seccion }}</td>
                  <td>{{ course.periodo }}</td>
                  <td>{{ course.docente || 'Sin asignar' }}</td>
                </tr>
                <tr *ngIf="!data.courses.length"><td colspan="5" class="text-center text-muted">No tienes cursos activos.</td></tr>
              </tbody>
            </table>
          </div>
        </section>

        <section *ngSwitchCase="'grades'" class="panel">
          <h3>Mis calificaciones ({{ data.grades.length }})</h3>
          <div class="table-responsive">
            <table class="table table-hover">
              <thead><tr><th>Curso</th><th>Evaluación</th><th>Fecha</th><th>Nota</th><th>Observación</th></tr></thead>
              <tbody>
                <tr *ngFor="let grade of data.grades">
                  <td>{{ grade.curso }}</td>
                  <td>{{ grade.evaluacion }}</td>
                  <td>{{ grade.fecha }}</td>
                  <td><span class="grade-pill" [class.low]="grade.nota < 11">{{ grade.nota }}</span></td>
                  <td>{{ grade.observacion || '—' }}</td>
                </tr>
                <tr *ngIf="!data.grades.length"><td colspan="5" class="text-center text-muted">Aún no tienes calificaciones registradas.</td></tr>
              </tbody>
            </table>
          </div>
        </section>

        <section *ngSwitchCase="'attendance'" class="panel">
          <h3>Mi asistencia ({{ data.attendance.length }} registros)</h3>
          <div class="table-responsive">
            <table class="table table-hover">
              <thead><tr><th>Fecha</th><th>Curso</th><th>Estado</th><th>Observación</th></tr></thead>
              <tbody>
                <tr *ngFor="let item of data.attendance">
                  <td>{{ item.fecha }}</td>
                  <td>{{ item.curso }}</td>
                  <td><span class="status" [class.ok]="item.estado === 'PRESENTE' || item.estado === 'JUSTIFICADO'" [class.warn]="item.estado === 'TARDANZA'" [class.bad]="item.estado === 'AUSENTE'">{{ item.estado }}</span></td>
                  <td>{{ item.observacion || '—' }}</td>
                </tr>
                <tr *ngIf="!data.attendance.length"><td colspan="4" class="text-center text-muted">Aún no tienes asistencias registradas.</td></tr>
              </tbody>
            </table>
          </div>
        </section>
      </ng-container>
    </ng-container>

    <ng-template #loading>
      <div *ngIf="errorMessage; else loadingMessage" class="alert alert-danger" role="alert">{{ errorMessage }}</div>
      <ng-template #loadingMessage><p class="text-muted" role="status">Cargando tu información...</p></ng-template>
    </ng-template>
  `,
  styles: [`
    .student-profile-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px 24px; }
    .student-profile-grid > div { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
    .student-profile-grid small { color: var(--muted); font-size: 12px; }
    .student-profile-grid strong { font-size: 14px; overflow-wrap: anywhere; }
    @media (max-width: 520px) { .student-profile-grid { grid-template-columns: 1fr; } }
  `]
})
export class StudentPortalComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  data: StudentSummary | null = null;
  section: StudentSection = 'profile';
  errorMessage = '';

  ngOnInit(): void {
    this.route.data.subscribe(routeData => {
      this.section = routeData['section'] as StudentSection;
    });
    this.api.studentSummary().subscribe({
      next: summary => this.data = summary,
      error: () => this.errorMessage = 'No se pudo cargar tu información. Inténtalo nuevamente.'
    });
  }

  get pageTitle(): string {
    return {
      profile: 'Mi perfil',
      courses: 'Mis cursos',
      grades: 'Mis calificaciones',
      attendance: 'Mi asistencia'
    }[this.section];
  }

  get pageDescription(): string {
    return {
      profile: 'Consulta tus datos personales y académicos.',
      courses: 'Consulta los cursos en los que estás matriculado.',
      grades: 'Revisa tus resultados de evaluaciones.',
      attendance: 'Consulta tus registros de asistencia.'
    }[this.section];
  }
}
