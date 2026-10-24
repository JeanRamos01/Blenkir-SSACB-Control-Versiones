import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({selector:'app-student-records',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Ficha académica del estudiante</h2><p>Consulta docente de estudiantes inscritos en sus cursos, calificaciones y asistencia.</p></div></div>
<div class="panel form-panel"><label class="form-label fw-semibold">Estudiante</label><select class="form-select" [(ngModel)]="studentId"><option [ngValue]="null">Selecciona un estudiante</option><option *ngFor="let s of students" [ngValue]="s.id">{{s.apellidos}}, {{s.nombres}} · {{s.grado}} {{s.seccion}}</option></select></div>
<ng-container *ngIf="selected"><div class="student-banner"><div><h3>{{selected.nombres}} {{selected.apellidos}}</h3><p>{{selected.grado}} · Sección {{selected.seccion}} · Código {{selected.codigo}}</p></div><span class="status" [class.ok]="selected.activo" [class.warn]="!selected.activo">{{selected.activo?'Activo':'Inactivo'}}</span></div>
<div class="split"><div class="panel"><h3>Calificaciones de mis cursos</h3><table class="table table-hover"><thead><tr><th>Curso</th><th>Evaluación</th><th>Nota</th></tr></thead><tbody><tr *ngFor="let g of selectedGrades"><td>{{g.curso}}</td><td>{{g.evaluacion}}</td><td><span class="grade-pill">{{g.nota}}</span></td></tr><tr *ngIf="!selectedGrades.length"><td colspan="3" class="text-muted">No hay notas visibles.</td></tr></tbody></table></div>
<div class="panel"><h3>Asistencia de mis cursos</h3><table class="table table-hover"><thead><tr><th>Fecha</th><th>Curso</th><th>Estado</th></tr></thead><tbody><tr *ngFor="let a of selectedAttendance"><td>{{a.fecha}}</td><td>{{a.curso}}</td><td>{{a.estado}}</td></tr><tr *ngIf="!selectedAttendance.length"><td colspan="3" class="text-muted">No hay asistencia visible.</td></tr></tbody></table></div></div></ng-container>`})
export class StudentRecordsComponent implements OnInit {
 private api=inject(ApiService);students:any[]=[];grades:any[]=[];attendance:any[]=[];studentId:number|null=null;
 ngOnInit(){this.api.students().subscribe(x=>this.students=x);this.api.grades().subscribe(x=>this.grades=x);this.api.attendance().subscribe(x=>this.attendance=x);}
 get selected(){return this.students.find(x=>x.id===this.studentId);}
 get selectedGrades(){return this.grades.filter(x=>x.estudianteId===this.studentId);}
 get selectedAttendance(){return this.attendance.filter(x=>x.estudianteId===this.studentId);}
}
