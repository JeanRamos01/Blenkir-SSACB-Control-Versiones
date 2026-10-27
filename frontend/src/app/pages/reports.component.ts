import { Component, inject, OnInit } from '@angular/core';import { CommonModule } from '@angular/common';import { FormsModule } from '@angular/forms';import { ApiService } from '../core/api.service';
@Component({selector:'app-reports',standalone:true,imports:[CommonModule,FormsModule],template:`<div class="page-head"><div><h2>Reportes académicos</h2><p>Consulta consolidada de calificaciones y asistencia.</p></div><button class="btn btn-outline-primary" onclick="window.print()"><i class="bi bi-printer"></i> Imprimir</button></div><section class="mb-4"><h3>Exportaciones Excel</h3><div class="export-grid"><div class="panel" *ngFor="let x of exports"><h4>{{x.title}}</h4><button class="btn btn-orange" [disabled]="exportBusy" (click)="downloadExport(x.key)"><i class="bi bi-file-earmark-excel"></i> Descargar Excel</button></div></div></section><div class="alert alert-danger" *ngIf="exportError">{{exportError}}</div><div class="panel filter-bar"><select class="form-select" [(ngModel)]="courseId" (change)="load()"><option [ngValue]="null">Todos los cursos</option><option *ngFor="let c of courses" [ngValue]="c.id">{{c.nombre}} · {{c.grado}} {{c.seccion}}</option></select></div><div class="split"><div class="panel"><h3>Calificaciones</h3><table class="table"><thead><tr><th>Estudiante</th><th>Curso</th><th>Evaluación</th><th>Nota</th></tr></thead><tbody><tr *ngFor="let x of grades"><td>{{x.estudiante}}</td><td>{{x.curso}}</td><td>{{x.evaluacion}}</td><td>{{x.nota}}</td></tr></tbody></table></div><div class="panel"><h3>Asistencia</h3><table class="table"><thead><tr><th>Fecha</th><th>Estudiante</th><th>Curso</th><th>Estado</th></tr></thead><tbody><tr *ngFor="let x of attendance"><td>{{x.fecha}}</td><td>{{x.estudiante}}</td><td>{{x.curso}}</td><td>{{x.estado}}</td></tr></tbody></table></div></div>`})
export class ReportsComponent implements OnInit{
	api=inject(ApiService);courses:any[]=[];grades:any[]=[];attendance:any[]=[];courseId:number|null=null;exportBusy=false;exportError='';
	exports:{key:'students'|'enrollments'|'competencies',title:string}[]=[
		{key:'students',title:'Padrón de estudiantes'},
		{key:'enrollments',title:'Matrículas por curso'},
		{key:'competencies',title:'Logros por competencias'}
	];
	ngOnInit(){this.api.courses().subscribe(x=>this.courses=x);this.load()}
	load(){this.api.grades(this.courseId||undefined).subscribe(x=>this.grades=x);this.api.attendance(this.courseId||undefined).subscribe(x=>this.attendance=x)}
	downloadExport(key:'students'|'enrollments'|'competencies'){
		const fileNames={students:'padron_estudiantes',enrollments:'matriculas_por_curso',competencies:'logros_por_competencias'};
		this.exportBusy=true;this.exportError='';
		this.api.downloadExport(key,'xlsx').subscribe({next:blob=>{
			const url=URL.createObjectURL(blob);const link=document.createElement('a');link.href=url;link.download=`${fileNames[key]}.xlsx`;
			document.body.appendChild(link);link.click();link.remove();URL.revokeObjectURL(url);this.exportBusy=false;
		},error:()=>{this.exportBusy=false;this.exportError='No se pudo descargar el Excel. Verifica tu sesión.';}});
	}
}
