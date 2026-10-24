import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
 selector:'app-layout',standalone:true,imports:[CommonModule,RouterOutlet,RouterLink,RouterLinkActive],
 template:`
 <div class="app-shell">
   <aside class="sidebar">
     <div class="brand-box"><img src="assets/logo.png" alt="Blenkir"><div><b>Colegios</b><strong>Blenkir</strong><small>¡Piensa en grande!</small></div></div>
     <nav>
       <a routerLink="/dashboard" routerLinkActive="active"><i class="bi bi-house-fill"></i> Inicio</a>
       <ng-container [ngSwitch]="auth.role">
         <ng-container *ngSwitchCase="'ADMINISTRADOR'">
           <a routerLink="/students" routerLinkActive="active"><i class="bi bi-people"></i> Estudiantes</a>
           <a routerLink="/teachers" routerLinkActive="active"><i class="bi bi-person-workspace"></i> Docentes</a>
           <a routerLink="/courses" routerLinkActive="active"><i class="bi bi-mortarboard"></i> Académico</a>
           <a routerLink="/enrollments" routerLinkActive="active"><i class="bi bi-journal-check"></i> Matrículas</a>
           <a routerLink="/curriculum" routerLinkActive="active"><i class="bi bi-diagram-3"></i> Carga curricular</a>
           <a routerLink="/schedules" routerLinkActive="active"><i class="bi bi-clock-history"></i> Horarios</a>
           <a routerLink="/guardians" routerLinkActive="active"><i class="bi bi-person-hearts"></i> Apoderados</a>
           <a routerLink="/evaluations" routerLinkActive="active"><i class="bi bi-clipboard2-check"></i> Evaluaciones</a>
           <a routerLink="/grades" routerLinkActive="active"><i class="bi bi-star"></i> Calificaciones</a>
           <a routerLink="/competencies" routerLinkActive="active"><i class="bi bi-award"></i> Competencias</a>
           <a routerLink="/student-records" routerLinkActive="active"><i class="bi bi-file-person"></i> Ficha del estudiante</a>
           <a routerLink="/attendance" routerLinkActive="active"><i class="bi bi-calendar-check"></i> Asistencia</a>
           <a routerLink="/reports" routerLinkActive="active"><i class="bi bi-bar-chart"></i> Reportes</a>
           <a routerLink="/staff" routerLinkActive="active"><i class="bi bi-person-badge"></i> Personal</a>
           <a routerLink="/treasury" routerLinkActive="active"><i class="bi bi-cash-stack"></i> Tesorería</a>
           <a routerLink="/cash" routerLinkActive="active"><i class="bi bi-wallet2"></i> Caja</a>
           <a routerLink="/siagie" routerLinkActive="active"><i class="bi bi-file-earmark-arrow-down"></i> SIAGIE / CSV</a>
           <a routerLink="/audit" routerLinkActive="active"><i class="bi bi-shield-check"></i> Auditoría</a>
         </ng-container>
         <ng-container *ngSwitchCase="'SECRETARIA'">
           <a routerLink="/enrollments" routerLinkActive="active"><i class="bi bi-journal-check"></i> Padrón / Matrículas</a>
           <a routerLink="/schedules" routerLinkActive="active"><i class="bi bi-clock-history"></i> Horarios</a>
           <a routerLink="/treasury" routerLinkActive="active"><i class="bi bi-cash-stack"></i> Tesorería</a>
           <a routerLink="/cash" routerLinkActive="active"><i class="bi bi-wallet2"></i> Caja</a>
           <a routerLink="/siagie" routerLinkActive="active"><i class="bi bi-file-earmark-arrow-down"></i> SIAGIE / CSV</a>
         </ng-container>
         <ng-container *ngSwitchCase="'DOCENTE'">
           <a routerLink="/courses" routerLinkActive="active"><i class="bi bi-book"></i> Mis cursos</a>
           <a routerLink="/schedules" routerLinkActive="active"><i class="bi bi-clock-history"></i> Mi horario</a>
           <a routerLink="/attendance" routerLinkActive="active"><i class="bi bi-calendar-check"></i> Asistencia</a>
           <a routerLink="/evaluations" routerLinkActive="active"><i class="bi bi-clipboard2-check"></i> Evaluaciones</a>
           <a routerLink="/grades" routerLinkActive="active"><i class="bi bi-star"></i> Calificaciones</a>
           <a routerLink="/competencies" routerLinkActive="active"><i class="bi bi-award"></i> Competencias</a>
           <a routerLink="/student-records" routerLinkActive="active"><i class="bi bi-file-person"></i> Ficha del estudiante</a>
           <a routerLink="/reports" routerLinkActive="active"><i class="bi bi-bar-chart"></i> Reportes</a>
         </ng-container>
         <ng-container *ngSwitchCase="'ESTUDIANTE'">
           <a routerLink="/student" routerLinkActive="active"><i class="bi bi-person"></i> Mi perfil</a>
           <a routerLink="/student" routerLinkActive="active"><i class="bi bi-book"></i> Mis cursos</a>
           <a routerLink="/student" routerLinkActive="active"><i class="bi bi-star"></i> Mis calificaciones</a>
           <a routerLink="/student" routerLinkActive="active"><i class="bi bi-calendar-check"></i> Mi asistencia</a>
           <a routerLink="/schedules" routerLinkActive="active"><i class="bi bi-clock-history"></i> Mi horario</a>
         </ng-container>
         <ng-container *ngSwitchCase="'APODERADO'">
           <a routerLink="/guardian" routerLinkActive="active"><i class="bi bi-person-hearts"></i> Mis hijos</a>
           <a routerLink="/guardian" routerLinkActive="active"><i class="bi bi-star"></i> Notas</a>
           <a routerLink="/guardian" routerLinkActive="active"><i class="bi bi-calendar-check"></i> Asistencia</a>
         </ng-container>
       </ng-container>
       <a routerLink="/notifications" routerLinkActive="active"><i class="bi bi-megaphone"></i> Comunicados</a>
     </nav>
     <div class="school-box"><i class="bi bi-building"></i><div><b>Colegio Blenkir</b><small>Año Escolar 2026</small></div></div>
   </aside>
   <section class="main-area">
     <header class="topbar">
       <div><h1>{{title}}</h1><span>Sistema de Seguimiento Académico (SSACB)</span></div>
       <div class="user"><div class="avatar">{{initials}}</div><div><b>{{auth.session?.displayName}}</b><small>{{roleLabel}}</small></div><button class="btn btn-sm btn-outline-secondary" (click)="logout()">Salir</button></div>
     </header>
     <main class="content"><router-outlet></router-outlet></main>
   </section>
 </div>`
})
export class LayoutComponent{
 auth=inject(AuthService); private router=inject(Router);
 get roleLabel(){return ({ADMINISTRADOR:'Administrador',SECRETARIA:'Secretaría',DOCENTE:'Docente',ESTUDIANTE:'Estudiante',APODERADO:'Apoderado'} as any)[this.auth.role]||this.auth.role;}
 get title(){return this.auth.role==='SECRETARIA'?'Módulo de Secretaría':this.auth.role==='DOCENTE'?'Módulo Docente':this.auth.role==='ESTUDIANTE'?'Portal del Estudiante':this.auth.role==='APODERADO'?'Portal del Apoderado':'Sistema de Seguimiento Académico (SSACB)';}
 get initials(){return (this.auth.session?.displayName||'U').split(' ').slice(0,2).map(x=>x[0]).join('').toUpperCase();}
 logout(){this.auth.logout();this.router.navigate(['/login']);}
}
