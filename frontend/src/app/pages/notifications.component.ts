import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({selector:'app-notifications',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Comunicados y notificaciones</h2><p>Tablón institucional de Colegio Blenkir por perfil de usuario.</p></div></div>
<div class="panel form-panel" *ngIf="auth.role==='ADMINISTRADOR'||auth.role==='SECRETARIA'"><h3>Publicar comunicado</h3><div class="row g-3"><div class="col-md-6"><input class="form-control" maxlength="120" placeholder="Título del comunicado" [(ngModel)]="form.titulo"></div><div class="col-md-6"><select class="form-select" [(ngModel)]="form.rolDestino"><option *ngFor="let r of roles" [value]="r">{{r}}</option></select></div><div class="col-12"><textarea class="form-control" rows="3" maxlength="3000" placeholder="Mensaje para la comunidad educativa" [(ngModel)]="form.mensaje"></textarea></div></div><button class="btn btn-orange mt-3" [disabled]="!form.titulo||!form.mensaje" (click)="save()">Publicar</button></div>
<div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
<div class="notice-grid"><article class="panel" *ngFor="let n of items"><div class="d-flex justify-content-between"><span class="status ok">{{n.rolDestino==='TODOS'?'Comunidad Blenkir':n.rolDestino}}</span><small class="text-muted">{{n.fecha|date:'mediumDate'}}</small></div><h3 class="mt-3">{{n.titulo}}</h3><p class="notice-body">{{n.mensaje}}</p><small class="text-muted">Publicado por {{n.autor}}</small></article></div>
<div class="panel text-muted" *ngIf="!items.length">No hay comunicados para tu perfil.</div>`})
export class NotificationsComponent implements OnInit {
  private api=inject(ApiService);auth=inject(AuthService);items:any[]=[];roles=['TODOS','ADMINISTRADOR','SECRETARIA','DOCENTE','ESTUDIANTE','APODERADO'];
  form:any={titulo:'',mensaje:'',rolDestino:'TODOS'};message='';error=false;
  ngOnInit(){this.load();}load(){this.api.notifications().subscribe(x=>this.items=x);}
  save(){this.api.postNotification(this.form).subscribe({next:()=>{this.form={titulo:'',mensaje:'',rolDestino:'TODOS'};this.message='Comunicado publicado.';this.error=false;this.load();},error:e=>{this.message=e?.error?.message||'No se pudo publicar.';this.error=true;}});}
}
