import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../core/api.service';

@Component({selector:'app-siagie',standalone:true,imports:[CommonModule],template:`
<div class="page-head"><div><h2>Preparación de datos para SIAGIE</h2><p>Exportaciones académicas de Blenkir en CSV UTF-8 para validación administrativa.</p></div></div>
<div class="alert alert-warning"><i class="bi bi-exclamation-triangle"></i> Los archivos son reportes preparatorios: comprueba el formato vigente y los campos oficiales de SIAGIE antes de importarlos. No se realiza integración ni envío automático al MINEDU.</div>
<div class="export-grid"><div class="panel" *ngFor="let x of exports"><i class="bi bi-filetype-csv export-icon"></i><h3>{{x.title}}</h3><p class="text-muted">{{x.description}}</p><button class="btn btn-orange" [disabled]="busy" (click)="download(x.key)"><i class="bi bi-download"></i> Descargar CSV</button></div></div>
<div class="alert alert-danger mt-3" *ngIf="error">{{error}}</div>`})
export class SiagieComponent {
  private api=inject(ApiService);busy=false;error='';
  exports:{key:'students'|'enrollments'|'competencies',title:string,description:string}[]=[
    {key:'students',title:'Padrón de estudiantes',description:'DNI, código, nombres, grado, sección y estado de matrícula institucional.'},
    {key:'enrollments',title:'Matrículas por curso',description:'Relación de estudiantes inscritos en áreas y periodos escolares.'},
    {key:'competencies',title:'Logros por competencias',description:'Escala AD/A/B/C, periodo y conclusiones descriptivas registradas.'}
  ];
  download(key:'students'|'enrollments'|'competencies'){
    this.busy=true;this.error='';this.api.downloadExport(key).subscribe({next:blob=>{
      const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download=`blenkir_${key}_siagie_preparacion.csv`;
      document.body.appendChild(a);a.click();a.remove();URL.revokeObjectURL(url);this.busy=false;
    },error:()=>{this.busy=false;this.error='No se pudo descargar el archivo. Verifica tu sesión.';}});
  }
}
