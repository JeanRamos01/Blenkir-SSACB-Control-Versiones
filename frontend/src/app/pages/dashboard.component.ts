import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
@Component({selector:'app-dashboard',standalone:true,imports:[CommonModule,RouterLink],template:`
<div class="page-head"><div><h2>¡Bienvenido, {{auth.session?.displayName}}!</h2><p>Resumen general del estado académico.</p></div><span class="period-chip"><i class="bi bi-calendar3"></i> Año escolar 2026</span></div>
<div class="metric-grid" *ngIf="data">
  <div class="metric-card purple"><i class="bi bi-people-fill"></i><div><span>{{label1}}</span><strong>{{value1}}</strong><small>{{sub1}}</small></div></div>
  <div class="metric-card green"><i class="bi bi-book-half"></i><div><span>{{label2}}</span><strong>{{value2}}</strong><small>{{sub2}}</small></div></div>
  <div class="metric-card blue"><i class="bi bi-person-workspace"></i><div><span>{{label3}}</span><strong>{{value3}}</strong><small>{{sub3}}</small></div></div>
  <div class="metric-card orange"><i class="bi bi-clipboard-check"></i><div><span>{{label4}}</span><strong>{{value4}}</strong><small>{{sub4}}</small></div></div>
</div>
<div class="dashboard-layout">
 <section class="panel"><div class="panel-title-row"><h3>Acciones rápidas</h3></div><div class="quick-grid">
   <a *ngIf="auth.role==='ADMINISTRADOR'" routerLink="/students"><i class="bi bi-person-plus"></i> Registrar estudiante</a>
   <a *ngIf="auth.role==='ADMINISTRADOR'" routerLink="/guardians"><i class="bi bi-person-hearts"></i> Gestionar apoderados</a>
   <a *ngIf="auth.role==='ADMINISTRADOR'||auth.role==='SECRETARIA'" routerLink="/enrollments"><i class="bi bi-journal-check"></i> Revisar matrículas</a>
   <a *ngIf="auth.role==='ADMINISTRADOR'||auth.role==='SECRETARIA'" routerLink="/treasury"><i class="bi bi-cash-stack"></i> Ir a tesorería</a>
   <a *ngIf="auth.role==='DOCENTE'" routerLink="/attendance"><i class="bi bi-calendar-check"></i> Registrar asistencia</a>
   <a *ngIf="auth.role==='DOCENTE'" routerLink="/grades"><i class="bi bi-star"></i> Registrar calificaciones</a>
   <a *ngIf="auth.role==='ESTUDIANTE'" routerLink="/student"><i class="bi bi-book"></i> Ver mis notas</a>
   <a *ngIf="auth.role==='ESTUDIANTE'" routerLink="/schedules"><i class="bi bi-clock-history"></i> Mi horario</a>
   <a *ngIf="auth.role==='APODERADO'" routerLink="/guardian"><i class="bi bi-search"></i> Consultar a mi hijo</a>
   <a routerLink="/notifications"><i class="bi bi-megaphone"></i> Ver comunicados</a>
 </div></section>
 <section class="panel"><h3>Privacidad y acceso</h3><p>El sistema aplica acceso por rol. Cada estudiante solo puede consultar sus propios datos y cada apoderado únicamente los de estudiantes previamente vinculados por la institución.</p><div class="security-banner"><i class="bi bi-shield-check"></i><span>El DNI no funciona como contraseña ni autoriza por sí solo el acceso.</span></div></section>
</div>`})
export class DashboardComponent implements OnInit {
 api=inject(ApiService);auth=inject(AuthService);data:any;
 ngOnInit(){this.api.dashboard().subscribe(x=>this.data=x);}
 get manager(){return this.auth.role==='ADMINISTRADOR'||this.auth.role==='SECRETARIA';}
 get label1(){return this.manager?'Estudiantes':this.auth.role==='DOCENTE'?'Mis cursos':this.auth.role==='ESTUDIANTE'?'Mis cursos':'Hijos vinculados';}
 get value1(){return this.data?.students??this.data?.courses??this.data?.children??0;}
 get sub1(){return 'Activos';}
 get label2(){return this.manager?'Cursos activos':this.auth.role==='DOCENTE'?'Estudiantes a cargo':this.auth.role==='ESTUDIANTE'?'Calificaciones':'Acceso autorizado';}
 get value2(){return this.manager?this.data?.courses:this.auth.role==='DOCENTE'?this.data?.students:this.auth.role==='ESTUDIANTE'?this.data?.grades:this.data?.children??0;}
 get sub2(){return 'Este periodo';}
 get label3(){return this.manager?'Docentes':'Seguridad';}
 get value3(){return this.manager?this.data?.teachers:'✓';}
 get sub3(){return 'Control por rol';}
 get label4(){return this.manager?'Calificaciones registradas':'Portal';}
 get value4(){return this.manager?this.data?.grades:'2026';}
 get sub4(){return 'SSACB';}
}
