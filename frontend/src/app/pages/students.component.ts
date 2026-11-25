import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  selector:'app-students',
  standalone:true,
  imports:[CommonModule,FormsModule],
  template:`<div class="page-head"><div><h2>Estudiantes</h2><p>Registro y consulta de estudiantes.</p></div><button class="btn btn-orange" (click)="show=!show; form=empty()">{{show?'Cerrar':'Nuevo estudiante'}}</button></div>
  <div class="panel form-panel" *ngIf="show"><h3>{{form.id?'Editar estudiante':'Registrar estudiante'}}</h3><div class="form-grid">
  <input class="form-control" placeholder="Código" [(ngModel)]="form.codigo"><input class="form-control" maxlength="8" placeholder="DNI (8 dígitos)" [(ngModel)]="form.dni">
  <input class="form-control" placeholder="Nombres" [(ngModel)]="form.nombres"><input class="form-control" placeholder="Apellidos" [(ngModel)]="form.apellidos">
  <input class="form-control" placeholder="Grado" [(ngModel)]="form.grado"><input class="form-control" placeholder="Sección" [(ngModel)]="form.seccion">
  <input class="form-control" placeholder="Correo" [(ngModel)]="form.email"><input class="form-control" placeholder="Dirección" [(ngModel)]="form.direccion"><input class="form-control" placeholder="Teléfono" [(ngModel)]="form.telefono">
  <input *ngIf="!form.id" class="form-control" placeholder="Usuario (opcional)" [(ngModel)]="form.username"><input *ngIf="!form.id" class="form-control" type="password" placeholder="Contraseña inicial" [(ngModel)]="form.password">
  </div><button class="btn btn-primary mt-3" [disabled]="!valid()" (click)="save()">Guardar</button></div>
  <div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
  <div class="panel"><div class="form-grid mb-3"><input class="form-control search-input" [(ngModel)]="query" placeholder="Buscar DNI, código, nombres o apellidos">
  <select class="form-select" [(ngModel)]="gradeFilter"><option value="">Todos los grados</option><option *ngFor="let g of grades" [value]="g">{{g}}</option></select>
  <select class="form-select" [(ngModel)]="sectionFilter"><option value="">Todas las secciones</option><option *ngFor="let s of sections" [value]="s">{{s}}</option></select>
  <select class="form-select" [(ngModel)]="statusFilter"><option value="">Todos los estados</option><option value="activo">Activos</option><option value="inactivo">Inactivos</option></select></div>
  <div class="table-responsive"><table class="table align-middle"><thead><tr><th>Código</th><th>Estudiante</th><th>DNI</th><th>Grado</th><th>Sección</th><th>Estado</th><th>Acciones</th></tr></thead>
  <tbody><tr *ngFor="let x of filtered"><td>{{x.codigo}}</td><td><b>{{x.nombres}} {{x.apellidos}}</b></td><td>{{x.dni}}</td><td>{{x.grado}}</td><td>{{x.seccion}}</td><td><span class="status" [class.ok]="x.activo" [class.warn]="!x.activo">{{x.activo?'Activo':'Inactivo'}}</span></td><td><button class="btn btn-sm btn-outline-primary me-1" (click)="edit(x)">Editar</button><button class="btn btn-sm" [class.btn-outline-secondary]="x.activo" [class.btn-outline-success]="!x.activo" (click)="toggle(x)">{{x.activo?'Desactivar':'Activar'}}</button></td></tr>
  <tr *ngIf="!filtered.length"><td colspan="7" class="text-center text-muted">No existen coincidencias.</td></tr></tbody></table></div></div>`
})
export class StudentsComponent implements OnInit {
  api=inject(ApiService);items:any[]=[];show=false;form:any={};query='';gradeFilter='';sectionFilter='';statusFilter='';message='';error=false;
  ngOnInit(){this.load();}
  empty(){return {codigo:'',dni:'',nombres:'',apellidos:'',grado:'',seccion:'',email:'',direccion:'',telefono:'',username:'',password:''};}
  load(){this.api.students().subscribe({next:x=>this.items=x,error:e=>this.fail(e)});}
  valid(){return this.form.codigo&&/^\d{8}$/.test(this.form.dni||'')&&this.form.nombres&&this.form.apellidos&&this.form.grado&&this.form.seccion;}
  get grades(){return [...new Set(this.items.map(x=>x.grado).filter(Boolean))].sort();}
  get sections(){return [...new Set(this.items.map(x=>x.seccion).filter(Boolean))].sort();}
  get filtered(){const q=this.normalize(this.query);return this.items.filter(x=>{
    const text=this.normalize(`${x.dni} ${x.codigo} ${x.nombres} ${x.apellidos}`);
    return (!q||text.includes(q))&&(!this.gradeFilter||x.grado===this.gradeFilter)&&(!this.sectionFilter||x.seccion===this.sectionFilter)&&(!this.statusFilter||(this.statusFilter==='activo'?x.activo:!x.activo));
  });}
  normalize(x:string){return (x||'').toLowerCase().trim().replace(/\s+/g,' ');}
  edit(x:any){this.form={...x,password:''};this.show=true;}
  save(){const req=this.form.id?this.api.updateStudent(this.form.id,this.form):this.api.addStudent(this.form);req.subscribe({next:()=>{this.success('Estudiante guardado.');this.form=this.empty();this.show=false;this.load();},error:e=>this.fail(e)});}
  toggle(x:any){this.api.studentStatus(x.id,!x.activo).subscribe({next:()=>this.load(),error:e=>this.fail(e)});}
  success(m:string){this.message=m;this.error=false;}fail(e:any){this.message=e?.error?.message||'No se pudo completar la operación.';this.error=true;}
}
