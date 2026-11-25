import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  selector:'app-user-management',
  standalone:true,
  imports:[CommonModule,FormsModule],
  template:`<div class="page-head"><div><h2>Usuarios y contraseñas</h2><p>Restablece contraseñas sin consultar ni revelar la contraseña anterior.</p></div></div>
  <div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
  <div class="panel mb-3"><input class="form-control search-input" [(ngModel)]="query" placeholder="Buscar usuario, nombre, rol, DNI o código"></div>
  <div class="panel"><div class="table-responsive"><table class="table table-hover"><thead><tr><th>Usuario</th><th>Nombre</th><th>Rol</th><th>Referencia</th><th>Estado</th><th></th></tr></thead><tbody>
  <tr *ngFor="let user of filteredUsers"><td>{{user.username}}</td><td>{{user.displayName}}</td><td>{{user.role}}</td><td>{{user.referencia||'—'}}</td><td>{{user.activo?'Activo':'Inactivo'}}</td><td><button class="btn btn-sm btn-outline-primary" (click)="select(user)">Restablecer contraseña</button></td></tr>
  <tr *ngIf="!filteredUsers.length"><td colspan="6" class="text-center text-muted">No hay usuarios coincidentes.</td></tr></tbody></table></div></div>
  <div class="panel mt-3" *ngIf="selected"><h3>Nueva contraseña para {{selected.username}}</h3><p class="small text-muted">Debe tener al menos 12 caracteres. Se mostrará una sola vez al guardar.</p>
  <div class="input-group"><input class="form-control" type="text" autocomplete="new-password" [(ngModel)]="password" aria-label="Nueva contraseña"><button class="btn btn-outline-secondary" (click)="generate()">Generar segura</button><button class="btn btn-primary" [disabled]="password.length<12" (click)="save()">Guardar nueva contraseña</button><button class="btn btn-outline-secondary" (click)="cancel()">Cancelar</button></div></div>
  <div class="alert alert-warning mt-3" *ngIf="displayedPassword"><strong>Comunícala ahora:</strong> {{displayedPassword}}<button class="btn btn-sm btn-outline-secondary ms-3" (click)="dismissPassword()">Ocultar</button></div>`
})
export class UserManagementComponent implements OnInit {
  private api=inject(ApiService);
  users:any[]=[];
  selected:any=null;
  query='';
  password='';
  displayedPassword='';
  message='';
  error=false;

  ngOnInit(){this.load();}
  load(){this.api.adminUsers(this.query).subscribe({next:users=>this.users=users,error:e=>this.fail(e)});}
  select(user:any){this.selected=user;this.password='';this.displayedPassword='';this.message='';}
  generate(){
    const alphabet='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-';
    const bytes=new Uint8Array(20);
    globalThis.crypto.getRandomValues(bytes);
    this.password=Array.from(bytes,b=>alphabet[b%alphabet.length]).join('');
  }
  save(){
    if(!this.selected||this.password.length<12)return;
    if(!globalThis.confirm(`¿Restablecer la contraseña de ${this.selected.username}?`))return;
    const oneTimePassword=this.password;
    this.api.resetUserPassword(this.selected.id,oneTimePassword).subscribe({
      next:()=>{this.password='';this.displayedPassword=oneTimePassword;this.message='Contraseña restablecida. La contraseña no se registra en auditoría.';this.error=false;this.selected=null;},
      error:e=>this.fail(e)
    });
  }
  cancel(){this.selected=null;this.password='';}
  dismissPassword(){this.displayedPassword='';}
  get filteredUsers(){const q=this.query.trim().toLowerCase();return this.users.filter(user=>
    !q||`${user.username} ${user.displayName} ${user.role} ${user.referencia||''}`.toLowerCase().includes(q));}
  private fail(e:any){this.message=e?.error?.message||'No se pudo completar la operación.';this.error=true;}
}
