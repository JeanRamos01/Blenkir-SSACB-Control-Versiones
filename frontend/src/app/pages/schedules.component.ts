import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({selector:'app-schedules',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>{{auth.role==='ESTUDIANTE'?'Mi horario de clases':'Horarios escolares'}}</h2><p>Sesiones semanales organizadas por curso, aula y hora.</p></div></div>
<div class="panel form-panel" *ngIf="auth.role==='ADMINISTRADOR'"><h3>Agregar sesión</h3><div class="form-grid">
<select class="form-select" [(ngModel)]="form.cursoId"><option [ngValue]="null">Curso</option><option *ngFor="let c of courses" [ngValue]="c.id">{{c.nombre}} · {{c.grado}} {{c.seccion}}</option></select>
<select class="form-select" [(ngModel)]="form.diaSemana"><option *ngFor="let d of days;index as i" [ngValue]="i+1">{{d}}</option></select>
<input class="form-control" type="time" [(ngModel)]="form.horaInicio" aria-label="Hora de inicio"><input class="form-control" type="time" [(ngModel)]="form.horaFin" aria-label="Hora final"><input class="form-control" placeholder="Aula" [(ngModel)]="form.aula">
<button class="btn btn-orange" [disabled]="!form.cursoId||!form.horaInicio||!form.horaFin" (click)="save()">Guardar horario</button></div></div>
<div *ngIf="message" class="alert alert-danger">{{message}}</div>
<div class="panel"><div class="table-responsive"><table class="table table-hover"><thead><tr><th>Día</th><th>Inicio</th><th>Fin</th><th>Curso</th><th>Grado y sección</th><th>Aula</th><th *ngIf="auth.role==='ADMINISTRADOR'">Acción</th></tr></thead><tbody>
<tr *ngFor="let h of items"><td>{{days[h.diaSemana-1]}}</td><td>{{h.horaInicio}}</td><td>{{h.horaFin}}</td><td>{{h.curso}}</td><td>{{h.grado}} · {{h.seccion}}</td><td>{{h.aula||'—'}}</td><td *ngIf="auth.role==='ADMINISTRADOR'"><button class="btn btn-sm btn-outline-danger" (click)="remove(h.id)">Eliminar</button></td></tr>
<tr *ngIf="!items.length"><td [attr.colspan]="auth.role==='ADMINISTRADOR'?7:6" class="text-center text-muted">Sin sesiones registradas.</td></tr></tbody></table></div></div>`})
export class SchedulesComponent implements OnInit {
  private api=inject(ApiService);auth=inject(AuthService);items:any[]=[];courses:any[]=[];days=['Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo'];message='';
  form:any={cursoId:null,diaSemana:1,horaInicio:'08:00',horaFin:'08:45',aula:''};
  ngOnInit(){this.load();if(this.auth.role==='ADMINISTRADOR')this.api.courses().subscribe(x=>this.courses=x);}
  load(){this.api.schedules().subscribe({next:x=>this.items=x,error:e=>this.message=e?.error?.message||'No se pudieron consultar los horarios.'});}
  save(){this.api.addSchedule(this.form).subscribe({next:()=>{this.message='';this.load();},error:e=>this.message=e?.error?.message||'No se pudo guardar.'});}
  remove(id:number){this.api.deleteSchedule(id).subscribe({next:()=>this.load(),error:e=>this.message=e?.error?.message||'No se pudo eliminar.'});}
}
