import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({selector:'app-staff',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Personal de Blenkir</h2><p>Registro de colaboradores administrativos y de servicio. El profesorado se gestiona en Docentes.</p></div><button class="btn btn-orange" (click)="editing=!editing">{{editing?'Cerrar':'Agregar personal'}}</button></div>
<div class="panel form-panel" *ngIf="editing"><h3>{{form.id?'Editar colaborador':'Nuevo colaborador'}}</h3><div class="form-grid">
<input class="form-control" placeholder="DNI (8 dígitos)" maxlength="8" [(ngModel)]="form.dni"><input class="form-control" placeholder="Nombre completo" [(ngModel)]="form.nombreCompleto">
<input class="form-control" placeholder="Cargo" [(ngModel)]="form.cargo"><input class="form-control" placeholder="Correo" type="email" [(ngModel)]="form.correo"><input class="form-control" placeholder="Teléfono" [(ngModel)]="form.telefono">
<label *ngIf="form.id" class="form-check mt-2"><input type="checkbox" class="form-check-input" [(ngModel)]="form.activo"> Activo</label><button class="btn btn-primary" [disabled]="!form.dni||!form.nombreCompleto||!form.cargo" (click)="save()">Guardar</button></div></div>
<div class="alert" *ngIf="message" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
<div class="panel form-panel"><table class="table table-hover"><thead><tr><th>DNI</th><th>Nombre</th><th>Cargo</th><th>Contacto</th><th>Estado</th><th></th></tr></thead><tbody><tr *ngFor="let x of items"><td>{{x.dni}}</td><td>{{x.nombreCompleto}}</td><td>{{x.cargo}}</td><td>{{x.correo}}<div>{{x.telefono}}</div></td><td><span class="status" [class.ok]="x.activo" [class.warn]="!x.activo">{{x.activo?'Activo':'Inactivo'}}</span></td><td><button class="btn btn-sm btn-outline-primary" (click)="edit(x)">Editar</button></td></tr></tbody></table></div>
<div class="panel"><h3>Accesos al módulo Secretaría</h3><p class="small text-muted">La creación de un colaborador no genera automáticamente una cuenta. Registra aquí el acceso de secretaría por separado.</p>
<div class="form-grid"><input class="form-control" placeholder="Usuario de Secretaría" [(ngModel)]="access.username"><input class="form-control" placeholder="Nombre mostrado" [(ngModel)]="access.displayName"><input class="form-control" type="password" placeholder="Contraseña de al menos 12 caracteres" [(ngModel)]="access.password"><button class="btn btn-orange" [disabled]="!access.username||!access.displayName||!access.password||access.password.length<12" (click)="createAccess()">Crear acceso</button></div>
<table class="table table-hover mt-3"><thead><tr><th>Usuario</th><th>Nombre</th><th>Estado</th><th></th></tr></thead><tbody><tr *ngFor="let u of secretariat"><td>{{u.username}}</td><td>{{u.displayName}}</td><td>{{u.activo?'Activo':'Inactivo'}}</td><td><button class="btn btn-sm btn-outline-secondary" (click)="toggleAccess(u)">{{u.activo?'Desactivar':'Activar'}}</button></td></tr></tbody></table></div>`})
export class StaffComponent implements OnInit {
  private api=inject(ApiService);items:any[]=[];secretariat:any[]=[];access:any={username:'',displayName:'',password:''};form:any={activo:true};editing=false;message='';error=false;
  ngOnInit(){this.load();}load(){this.api.staff().subscribe(x=>this.items=x);this.api.secretariatUsers().subscribe(x=>this.secretariat=x);}
  edit(x:any){this.form={...x};this.editing=true;}
  save(){this.api.saveStaff(this.form).subscribe({next:()=>{this.form={activo:true};this.editing=false;this.error=false;this.message='Registro guardado.';this.load();},error:e=>{this.error=true;this.message=e?.error?.message||'No se pudo guardar.';}});}
  createAccess(){this.api.createSecretariatUser(this.access).subscribe({next:()=>{this.access={username:'',displayName:'',password:''};this.error=false;this.message='Acceso de Secretaría creado.';this.load();},error:e=>{this.error=true;this.message=e?.error?.message||'No se pudo crear el acceso.';}});}
  toggleAccess(u:any){this.api.secretariatStatus(u.id,!u.activo).subscribe({next:()=>this.load(),error:e=>{this.error=true;this.message=e?.error?.message||'No se pudo cambiar el acceso.';}});}
}
