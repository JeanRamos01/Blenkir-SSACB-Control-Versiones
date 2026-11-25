import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({selector:'app-teachers',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Docentes</h2><p>Administración del personal docente.</p></div><button class="btn btn-orange" (click)="show=!show;form=empty()">{{show?'Cerrar':'Nuevo docente'}}</button></div>
<div class="panel" *ngIf="show"><div class="form-grid">
<input class="form-control" placeholder="Código" [(ngModel)]="form.codigo"><input class="form-control" maxlength="8" placeholder="DNI" [(ngModel)]="form.dni">
<input class="form-control" placeholder="Nombres" [(ngModel)]="form.nombres"><input class="form-control" placeholder="Apellidos" [(ngModel)]="form.apellidos">
<input class="form-control" placeholder="Especialidad" [(ngModel)]="form.especialidad"><input class="form-control" placeholder="Correo" [(ngModel)]="form.email">
<input *ngIf="!form.id" class="form-control" placeholder="Usuario" [(ngModel)]="form.username"><input *ngIf="!form.id" class="form-control" type="password" placeholder="Contraseña inicial" [(ngModel)]="form.password"></div>
<button class="btn btn-primary mt-3" [disabled]="!valid()" (click)="save()">Guardar</button></div>
<div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
<div class="panel"><div class="form-grid mb-3"><input class="form-control search-input" [(ngModel)]="query" placeholder="Buscar DNI, código, nombre o especialidad">
<select class="form-select" [(ngModel)]="specialty"><option value="">Todas las especialidades</option><option *ngFor="let s of specialties" [value]="s">{{s}}</option></select>
<select class="form-select" [(ngModel)]="status"><option value="">Todos los estados</option><option value="activo">Activos</option><option value="inactivo">Inactivos</option></select></div>
<table class="table align-middle"><thead><tr><th>Código</th><th>Docente</th><th>DNI</th><th>Especialidad</th><th>Correo</th><th>Estado</th><th>Acciones</th></tr></thead>
<tbody><tr *ngFor="let x of filtered"><td>{{x.codigo}}</td><td>{{x.nombres}} {{x.apellidos}}</td><td>{{x.dni}}</td><td>{{x.especialidad}}</td><td>{{x.email}}</td><td><span class="status" [class.ok]="x.activo" [class.warn]="!x.activo">{{x.activo?'Activo':'Inactivo'}}</span></td><td><button class="btn btn-sm btn-outline-primary me-1" (click)="edit(x)">Editar</button><button class="btn btn-sm btn-outline-secondary" (click)="toggle(x)">{{x.activo?'Desactivar':'Activar'}}</button></td></tr>
<tr *ngIf="!filtered.length"><td colspan="7" class="text-center text-muted">No existen coincidencias.</td></tr></tbody></table></div>`})
export class TeachersComponent implements OnInit {
  api=inject(ApiService);items:any[]=[];show=false;form:any={};query='';specialty='';status='';message='';error=false;
  ngOnInit(){this.load();}empty(){return {codigo:'',dni:'',nombres:'',apellidos:'',especialidad:'',email:'',username:'',password:''};}
  load(){this.api.teachers().subscribe({next:x=>this.items=x,error:e=>this.fail(e)});}
  valid(){return this.form.codigo&&/^\d{8}$/.test(this.form.dni||'')&&this.form.nombres&&this.form.apellidos;}
  get specialties(){return [...new Set(this.items.map(x=>x.especialidad).filter(Boolean))].sort();}
  get filtered(){const q=(this.query||'').toLowerCase().trim();return this.items.filter(x=>{
    const text=`${x.dni} ${x.codigo} ${x.nombres} ${x.apellidos} ${x.especialidad}`.toLowerCase();
    return (!q||text.includes(q))&&(!this.specialty||x.especialidad===this.specialty)&&(!this.status||(this.status==='activo'?x.activo:!x.activo));
  });}
  edit(x:any){this.form={...x,password:''};this.show=true;}
  save(){const req=this.form.id?this.api.updateTeacher(this.form.id,this.form):this.api.addTeacher(this.form);req.subscribe({next:()=>{this.message='Docente guardado.';this.error=false;this.show=false;this.form=this.empty();this.load();},error:e=>this.fail(e)});}
  toggle(x:any){this.api.teacherStatus(x.id,!x.activo).subscribe({next:()=>this.load(),error:e=>this.fail(e)});}
  fail(e:any){this.message=e?.error?.message||'No se pudo completar la operación.';this.error=true;}
}
