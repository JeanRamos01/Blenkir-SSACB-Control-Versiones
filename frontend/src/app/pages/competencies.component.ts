import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({selector:'app-competencies',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Evaluación por competencias</h2><p>Registro de logros AD, A, B y C con conclusiones descriptivas para Blenkir.</p></div></div>
<div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
<div class="split form-panel"><section class="panel"><h3>Registrar competencia</h3><div class="d-grid gap-2">
<select class="form-select" [(ngModel)]="newCompetency.cursoId"><option [ngValue]="null">Seleccionar curso</option><option *ngFor="let c of courses" [ngValue]="c.id">{{c.nombre}} · {{c.grado}} {{c.seccion}}</option></select>
<input class="form-control" placeholder="Nombre de competencia" [(ngModel)]="newCompetency.nombre"><textarea class="form-control" rows="2" placeholder="Descripción (opcional)" [(ngModel)]="newCompetency.descripcion"></textarea>
<button class="btn btn-orange" [disabled]="!newCompetency.cursoId||!newCompetency.nombre" (click)="create()">Registrar competencia</button></div></section>
<section class="panel"><h3>Registrar nivel de logro</h3><div class="d-grid gap-2">
<select class="form-select" [(ngModel)]="result.competenciaId" (ngModelChange)="onCompetencyChange()"><option [ngValue]="null">Competencia</option><option *ngFor="let x of competencies" [ngValue]="x.id">{{x.curso}} · {{x.nombre}}</option></select>
<select class="form-select" [(ngModel)]="result.estudianteId"><option [ngValue]="null">Estudiante matriculado</option><option *ngFor="let s of enrolled" [ngValue]="s.id">{{s.nombres}} {{s.apellidos}}</option></select>
<div class="d-flex gap-2"><input class="form-control" placeholder="Periodo (ej. 2026-B1)" [(ngModel)]="result.periodo"><select class="form-select" [(ngModel)]="result.nivel"><option *ngFor="let x of ['AD','A','B','C']" [value]="x">{{x}}</option></select></div>
<textarea class="form-control" rows="2" placeholder="Conclusión descriptiva" [(ngModel)]="result.conclusion"></textarea><button class="btn btn-primary" [disabled]="!result.competenciaId||!result.estudianteId||!result.periodo" (click)="saveResult()">Guardar logro</button></div></section></div>
<div class="panel"><h3>Resultados ({{results.length}})</h3><div class="table-responsive"><table class="table table-hover"><thead><tr><th>Estudiante</th><th>Competencia</th><th>Periodo</th><th>Nivel</th><th>Conclusión descriptiva</th></tr></thead><tbody>
<tr *ngFor="let r of results"><td>{{r.estudiante}}</td><td>{{r.competencia}}</td><td>{{r.periodo}}</td><td><span class="grade-pill">{{r.nivel}}</span></td><td>{{r.conclusion||'—'}}</td></tr><tr *ngIf="!results.length"><td colspan="5" class="text-center text-muted">No hay resultados todavía.</td></tr></tbody></table></div></div>`})
export class CompetenciesComponent implements OnInit {
  private api=inject(ApiService);courses:any[]=[];competencies:any[]=[];results:any[]=[];enrolled:any[]=[];
  newCompetency:any={cursoId:null,nombre:'',descripcion:''};result:any={competenciaId:null,estudianteId:null,periodo:'2026-B1',nivel:'A',conclusion:''};
  message='';error=false;
  ngOnInit(){this.api.courses().subscribe(x=>this.courses=x);this.load();}
  load(){this.api.competencies().subscribe(x=>this.competencies=x);this.api.competencyResults().subscribe(x=>this.results=x);}
  onCompetencyChange(){this.result.estudianteId=null;this.enrolled=[];const c=this.competencies.find(x=>x.id===this.result.competenciaId);if(c)this.api.courseStudents(c.cursoId).subscribe(x=>this.enrolled=x);}
  create(){this.api.createCompetency(this.newCompetency).subscribe({next:()=>{this.message='Competencia creada.';this.error=false;this.newCompetency={cursoId:null,nombre:'',descripcion:''};this.load();},error:e=>this.failure(e)});}
  saveResult(){this.api.saveCompetencyResult(this.result).subscribe({next:()=>{this.message='Nivel de logro guardado.';this.error=false;this.load();},error:e=>this.failure(e)});}
  failure(e:any){this.message=e?.error?.message||'No se pudo guardar.';this.error=true;}
}
