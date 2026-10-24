import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';

@Component({selector:'app-curriculum',standalone:true,imports:[CommonModule,FormsModule,RouterLink],template:`
<div class="page-head"><div><h2>Carga curricular</h2><p>Distribución de áreas académicas y docentes por sección, inspirada en SIGA UC.</p></div><a routerLink="/courses" class="btn btn-orange">Gestionar cursos</a></div>
<div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
<div class="panel"><div class="table-responsive"><table class="table table-hover"><thead><tr><th>Código</th><th>Área / Curso</th><th>Grado / Sección</th><th>Periodo</th><th>Docente asignado</th><th></th></tr></thead>
<tbody><tr *ngFor="let c of courses"><td>{{c.codigo}}</td><td class="fw-semibold">{{c.nombre}}</td><td>{{c.grado}} · {{c.seccion}}</td><td>{{c.periodo}}</td>
<td><select class="form-select form-select-sm" [(ngModel)]="c.docenteId"><option [ngValue]="null">Sin docente</option><option *ngFor="let t of teachers" [ngValue]="t.id">{{t.nombres}} {{t.apellidos}}</option></select></td>
<td><button class="btn btn-sm btn-outline-primary" (click)="assign(c)">Guardar</button></td></tr>
<tr *ngIf="!courses.length"><td colspan="6" class="text-muted text-center">Crea cursos para organizar la carga curricular.</td></tr></tbody></table></div></div>`})
export class CurriculumComponent implements OnInit {
  private api=inject(ApiService);courses:any[]=[];teachers:any[]=[];message='';error=false;
  ngOnInit(){this.load();this.api.teachers().subscribe(x=>this.teachers=x);}
  load(){this.api.curriculum().subscribe(x=>this.courses=x);}
  assign(c:any){this.api.assignTeacher(c.id,c.docenteId).subscribe({next:()=>{this.error=false;this.message='Asignación actualizada.';this.load();},error:e=>{this.error=true;this.message=e?.error?.message||'No se pudo asignar el docente.';}});}
}
