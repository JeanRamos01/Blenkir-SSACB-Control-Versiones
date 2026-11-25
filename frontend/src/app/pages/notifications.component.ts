import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({selector:'app-notifications',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Comunicados y notificaciones</h2><p>Tablón institucional de Colegio Blenkir por perfil de usuario.</p></div></div>
<div class="panel form-panel" *ngIf="auth.role==='ADMINISTRADOR'||auth.role==='SECRETARIA'"><h3>{{form.id?'Editar comunicado':'Publicar comunicado'}}</h3><div class="row g-3"><div class="col-md-6"><input class="form-control" maxlength="120" placeholder="Título del comunicado" [(ngModel)]="form.titulo"></div><div class="col-md-6"><select class="form-select" [(ngModel)]="form.rolDestino"><option *ngFor="let r of roles" [value]="r">{{r}}</option></select></div><div class="col-12"><textarea class="form-control" rows="3" maxlength="3000" placeholder="Mensaje para la comunidad educativa" [(ngModel)]="form.mensaje"></textarea></div></div><button class="btn btn-orange mt-3" [disabled]="!form.titulo||!form.mensaje" (click)="save()">Publicar</button></div>
<div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
<div class="panel mb-3"><div class="form-grid"><input class="form-control search-input" [(ngModel)]="query" placeholder="Buscar título o contenido"><select class="form-select" [(ngModel)]="roleFilter"><option value="">Todos los destinatarios</option><option *ngFor="let r of roles" [value]="r">{{r}}</option></select></div></div>
<div class="notice-grid"><article class="panel" *ngFor="let n of filtered"><div class="d-flex justify-content-between"><span class="status ok">{{n.rolDestino==='TODOS'?'Comunidad Blenkir':n.rolDestino}}</span><small class="text-muted">{{n.fecha|date:'mediumDate'}}</small></div><h3 class="mt-3">{{n.titulo}}</h3><p class="notice-body">{{n.mensaje}}</p><small class="text-muted">Publicado por {{n.autor}}</small><div class="mt-3" *ngIf="auth.role==='ADMINISTRADOR'"><button class="btn btn-sm btn-outline-primary me-1" (click)="edit(n)">Editar</button><button class="btn btn-sm btn-outline-danger" (click)="remove(n)">Eliminar</button></div></article></div>
<div class="panel text-muted" *ngIf="!filtered.length">No existen coincidencias.</div>`})
export class NotificationsComponent implements OnInit {
  private api=inject(ApiService);auth=inject(AuthService);items:any[]=[];roles=['TODOS','ADMINISTRADOR','SECRETARIA','DOCENTE','ESTUDIANTE','APODERADO'];
  form:any={titulo:'',mensaje:'',rolDestino:'TODOS'};message='';error=false;query='';roleFilter='';
  ngOnInit(){this.load();}load(){this.api.notifications().subscribe(x=>this.items=x);}
  get filtered(){const q=(this.query||'').toLowerCase().trim();return this.items.filter(n=>{const text=`${n.titulo} ${n.mensaje}`.toLowerCase();return (!q||text.includes(q))&&(!this.roleFilter||n.rolDestino===this.roleFilter);});}
  edit(n:any){this.form={id:n.id,titulo:n.titulo,mensaje:n.mensaje,rolDestino:n.rolDestino};}
  remove(n:any){this.api.deleteNotification(n.id).subscribe({next:()=>{this.message='Comunicado eliminado.';this.error=false;this.load();},error:e=>{this.message=e?.error?.message||'No se pudo eliminar.';this.error=true;}});}
  save(){const req=this.form.id?this.api.updateNotification(this.form.id,this.form):this.api.postNotification(this.form);req.subscribe({next:()=>{this.form={titulo:'',mensaje:'',rolDestino:'TODOS'};this.message='Comunicado guardado.';this.error=false;this.load();},error:e=>{this.message=e?.error?.message||'No se pudo publicar.';this.error=true;}});}
}
