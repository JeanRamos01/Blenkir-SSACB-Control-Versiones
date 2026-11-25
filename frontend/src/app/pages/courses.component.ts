import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({selector:'app-courses',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>{{auth.role==='DOCENTE'?'Mis cursos':'Gestión académica'}}</h2><p>Cursos, grados, secciones y docentes responsables.</p></div><button *ngIf="auth.role==='ADMINISTRADOR'" class="btn btn-orange" (click)="show=!show;form=empty()">Nuevo curso</button></div>
<div class="panel" *ngIf="show&&auth.role==='ADMINISTRADOR'"><div class="form-grid"><input class="form-control" placeholder="Código" [(ngModel)]="form.codigo"><input class="form-control" placeholder="Nombre" [(ngModel)]="form.nombre">
<input class="form-control" placeholder="Grado" [(ngModel)]="form.grado"><input class="form-control" placeholder="Sección" [(ngModel)]="form.seccion"><input class="form-control" placeholder="Periodo" [(ngModel)]="form.periodo">
<select class="form-select" [(ngModel)]="form.docenteId"><option [ngValue]="null">Docente</option><option *ngFor="let t of teachers" [ngValue]="t.id">{{t.nombres}} {{t.apellidos}}</option></select></div><button class="btn btn-primary mt-3" [disabled]="!form.codigo||!form.nombre" (click)="save()">Guardar</button></div>
<div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
<div class="panel mb-3"><div class="form-grid"><input class="form-control search-input" [(ngModel)]="query" placeholder="Buscar nombre, código, grado, sección o periodo">
<select class="form-select" [(ngModel)]="teacherFilter"><option value="">Todos los docentes</option><option *ngFor="let t of teachers" [value]="t.nombres+' '+t.apellidos">{{t.nombres}} {{t.apellidos}}</option></select>
<select class="form-select" [(ngModel)]="statusFilter"><option value="">Todos los estados</option><option value="activo">Activos</option><option value="inactivo">Inactivos</option></select></div></div>
<div class="course-grid"><div class="course-card" *ngFor="let c of filtered"><div class="course-icon"><i class="bi bi-book-half"></i></div><div><h3>{{c.nombre}}</h3><p>{{c.grado}} {{c.seccion}} · {{c.periodo}}</p><small>{{c.docente||'Sin docente asignado'}} · {{c.activo?'Activo':'Inactivo'}}</small><div class="mt-2" *ngIf="auth.role==='ADMINISTRADOR'"><button class="btn btn-sm btn-outline-primary me-1" (click)="edit(c)">Editar</button><button class="btn btn-sm btn-outline-secondary" (click)="toggle(c)">{{c.activo?'Desactivar':'Activar'}}</button></div></div></div></div>
<div class="panel mt-3" *ngIf="!filtered.length">No existen coincidencias.</div>
<div class="panel mt-3" *ngIf="auth.role==='ADMINISTRADOR'"><h3>Matricular estudiante en curso</h3><div class="form-grid"><select class="form-select" [(ngModel)]="enrollCourseId"><option [ngValue]="null">Curso</option><option *ngFor="let c of items" [ngValue]="c.id">{{c.nombre}} · {{c.grado}} {{c.seccion}}</option></select><select class="form-select" [(ngModel)]="enrollStudentId"><option [ngValue]="null">Estudiante</option><option *ngFor="let s of students" [ngValue]="s.id">{{s.nombres}} {{s.apellidos}}</option></select><button class="btn btn-primary" [disabled]="!enrollCourseId||!enrollStudentId" (click)="enroll()">Matricular</button></div></div>`})
export class CoursesComponent implements OnInit {
  api=inject(ApiService);auth=inject(AuthService);items:any[]=[];teachers:any[]=[];students:any[]=[];show=false;form:any={};enrollCourseId:number|null=null;enrollStudentId:number|null=null;query='';teacherFilter='';statusFilter='';message='';error=false;
  ngOnInit(){this.load();if(this.auth.role==='ADMINISTRADOR'){this.api.teachers().subscribe(x=>this.teachers=x);this.api.students().subscribe(x=>this.students=x);} }
  empty(){return {codigo:'',nombre:'',grado:'',seccion:'',periodo:'2026-I',docenteId:null};}
  load(){this.api.courses().subscribe({next:x=>this.items=x,error:e=>this.fail(e)});}
  get filtered(){const q=(this.query||'').toLowerCase().trim();return this.items.filter(c=>{
    const text=`${c.nombre} ${c.codigo} ${c.grado} ${c.seccion} ${c.periodo}`.toLowerCase();
    return (!q||text.includes(q))&&(!this.teacherFilter||c.docente===this.teacherFilter)&&(!this.statusFilter||(this.statusFilter==='activo'?c.activo:!c.activo));
  });}
  edit(c:any){this.form={...c,docenteId:c.docenteId};this.show=true;}
  save(){const req=this.form.id?this.api.updateCourse(this.form.id,this.form):this.api.addCourse(this.form);req.subscribe({next:()=>{this.message='Curso guardado.';this.error=false;this.show=false;this.load();},error:e=>this.fail(e)});}
  toggle(c:any){this.api.courseStatus(c.id,!c.activo).subscribe({next:()=>this.load(),error:e=>this.fail(e)});}
  enroll(){this.api.enroll(this.enrollCourseId!,this.enrollStudentId!).subscribe({next:()=>{this.message='Estudiante matriculado.';this.error=false;},error:e=>this.fail(e)});}
  fail(e:any){this.message=e?.error?.message||'No se pudo completar la operación.';this.error=true;}
}
