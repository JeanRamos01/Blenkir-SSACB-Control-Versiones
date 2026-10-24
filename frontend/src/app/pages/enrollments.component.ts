import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({selector:'app-enrollments',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Matrículas y padrón</h2><p>Inscripciones por curso, grado, sección y periodo escolar.</p></div><span class="period-chip">Blenkir · Control administrativo</span></div>
<div class="panel form-panel"><h3>Matricular estudiante</h3>
  <div class="form-grid"><select class="form-select" [(ngModel)]="studentId"><option [ngValue]="null">Seleccionar estudiante</option><option *ngFor="let s of students" [ngValue]="s.id">{{s.nombres}} {{s.apellidos}} · {{s.grado}} {{s.seccion}}</option></select>
  <select class="form-select" [(ngModel)]="courseId"><option [ngValue]="null">Seleccionar curso</option><option *ngFor="let c of courses" [ngValue]="c.id">{{c.nombre}} · {{c.grado}} {{c.seccion}}</option></select>
  <button class="btn btn-orange" [disabled]="saving||!studentId||!courseId" (click)="save()">{{saving?'Guardando…':'Confirmar matrícula'}}</button></div>
</div>
<div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error" role="status">{{message}}</div>
<div class="panel"><div class="d-flex justify-content-between align-items-center mb-3"><h3 class="mb-0">Matrículas registradas ({{filtered.length}})</h3><input class="form-control search-input" placeholder="Buscar estudiante o curso" [(ngModel)]="query"></div>
 <div class="table-responsive"><table class="table table-hover"><thead><tr><th>Estudiante</th><th>Curso</th><th>Grado / Sección</th><th>Estado</th><th>Acción</th></tr></thead>
 <tbody><tr *ngFor="let x of filtered"><td>{{x.estudiante}}</td><td>{{x.curso}}</td><td>{{x.grado}} · {{x.seccion}}</td><td><span class="status" [class.ok]="x.activo" [class.warn]="!x.activo">{{x.activo?'Activa':'Inactiva'}}</span></td><td><button class="btn btn-sm btn-outline-secondary" (click)="status(x)">{{x.activo?'Desactivar':'Reactivar'}}</button></td></tr>
 <tr *ngIf="!filtered.length"><td colspan="5" class="text-center text-muted">No hay matrículas con ese filtro.</td></tr></tbody></table></div>
</div>`})
export class EnrollmentsComponent implements OnInit {
  private api=inject(ApiService);students:any[]=[];courses:any[]=[];items:any[]=[];
  studentId:number|null=null;courseId:number|null=null;query='';message='';error=false;saving=false;
  ngOnInit(){this.load();this.api.students().subscribe(x=>this.students=x);this.api.courses().subscribe(x=>this.courses=x);}
  get filtered(){const q=this.query.toLowerCase().trim();return this.items.filter(x=>(x.estudiante+' '+x.curso).toLowerCase().includes(q));}
  load(){this.api.enrollmentList().subscribe({next:x=>this.items=x,error:e=>this.failure(e)});}
  save(){if(!this.studentId||!this.courseId)return;this.saving=true;this.api.registerEnrollment(this.studentId,this.courseId).subscribe({next:()=>{this.saving=false;this.message='Matrícula guardada correctamente.';this.error=false;this.load();},error:e=>{this.saving=false;this.failure(e);}});}
  status(x:any){this.api.enrollmentStatus(x.id,!x.activo).subscribe({next:()=>this.load(),error:e=>this.failure(e)});}
  failure(e:any){this.error=true;this.message=e?.error?.message||'No se pudo completar la operación.';}
}
