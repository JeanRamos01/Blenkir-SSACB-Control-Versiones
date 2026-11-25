import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({selector:'app-guardians',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Apoderados</h2><p>Registra al apoderado y vincúlalo con el estudiante autorizado.</p></div><button class="btn btn-orange" (click)="show=!show;form=empty()">{{show?'Cerrar':'Nuevo apoderado'}}</button></div>
<div class="panel" *ngIf="show"><h3>{{form.id?'Editar apoderado':'Registrar apoderado'}}</h3><div class="form-grid">
<input class="form-control" maxlength="8" placeholder="DNI del apoderado" [(ngModel)]="form.dni"><input class="form-control" placeholder="Nombres" [(ngModel)]="form.nombres"><input class="form-control" placeholder="Apellidos" [(ngModel)]="form.apellidos">
<input class="form-control" placeholder="Teléfono" [(ngModel)]="form.telefono"><input class="form-control" placeholder="Correo" [(ngModel)]="form.email">
<input *ngIf="!form.id" class="form-control" placeholder="Usuario" [(ngModel)]="form.username"><input *ngIf="!form.id" class="form-control" type="password" placeholder="Contraseña inicial" [(ngModel)]="form.password"></div>
<button class="btn btn-primary mt-3" [disabled]="!valid()" (click)="save()">Guardar</button></div>
<div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
<div class="panel mb-3"><input class="form-control search-input" [(ngModel)]="query" placeholder="Buscar DNI, nombres, apellidos o teléfono"></div>
<div class="split"><div class="panel"><h3>Apoderados registrados</h3><table class="table"><thead><tr><th>DNI</th><th>Apoderado</th><th>Contacto</th><th>Estado</th><th>Acciones</th></tr></thead>
<tbody><tr *ngFor="let x of filtered" (click)="selectedGuardian=x" [class.table-active]="selectedGuardian?.id===x.id"><td>{{x.dni}}</td><td>{{x.nombres}} {{x.apellidos}}</td><td>{{x.telefono}}</td><td><span class="status" [class.ok]="x.activo" [class.warn]="!x.activo">{{x.activo?'Activo':'Inactivo'}}</span></td><td><button class="btn btn-sm btn-outline-primary me-1" (click)="edit(x);$event.stopPropagation()">Editar</button><button class="btn btn-sm btn-outline-secondary" (click)="toggle(x);$event.stopPropagation()">{{x.activo?'Desactivar':'Activar'}}</button></td></tr>
<tr *ngIf="!filtered.length"><td colspan="5" class="text-center text-muted">No existen coincidencias.</td></tr></tbody></table></div>
<div class="panel"><h3>Vincular estudiante</h3><p class="small text-muted">Solo se habilita el acceso cuando la representación es autorizada.</p><select class="form-select mb-2" [(ngModel)]="studentId"><option [ngValue]="null">Estudiante</option><option *ngFor="let s of students" [ngValue]="s.id">{{s.nombres}} {{s.apellidos}} · DNI {{s.dni}}</option></select><input class="form-control mb-2" placeholder="Parentesco" [(ngModel)]="parentesco"><button class="btn btn-primary" [disabled]="!selectedGuardian||!studentId" (click)="link()">Vincular de forma autorizada</button></div></div>`})
export class GuardiansComponent implements OnInit {
  api=inject(ApiService);items:any[]=[];students:any[]=[];show=false;form:any={};selectedGuardian:any;studentId:number|null=null;parentesco='Apoderado';query='';message='';error=false;
  ngOnInit(){this.load();this.api.students().subscribe(x=>this.students=x);}
  empty(){return {dni:'',nombres:'',apellidos:'',telefono:'',email:'',username:'',password:''};}
  valid(){return /^\d{8}$/.test(this.form.dni||'')&&this.form.nombres&&this.form.apellidos;}
  get filtered(){const q=(this.query||'').toLowerCase().trim();return this.items.filter(x=>`${x.dni} ${x.nombres} ${x.apellidos} ${x.telefono||''}`.toLowerCase().includes(q));}
  load(){this.api.guardians().subscribe({next:x=>this.items=x,error:e=>this.fail(e)});}
  edit(x:any){this.form={...x,password:''};this.show=true;}
  save(){const req=this.form.id?this.api.updateGuardian(this.form.id,this.form):this.api.addGuardian(this.form);req.subscribe({next:()=>{this.message='Apoderado guardado.';this.error=false;this.show=false;this.load();},error:e=>this.fail(e)});}
  toggle(x:any){this.api.guardianStatus(x.id,!x.activo).subscribe({next:()=>this.load(),error:e=>this.fail(e)});}
  link(){this.api.linkGuardian(this.selectedGuardian.id,this.studentId!,this.parentesco).subscribe({next:()=>{this.message='Vínculo autorizado registrado.';this.error=false;},error:e=>this.fail(e)});}
  fail(e:any){this.message=e?.error?.message||'No se pudo completar la operación.';this.error=true;}
}
