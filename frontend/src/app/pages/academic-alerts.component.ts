import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({
  selector:'app-academic-alerts',
  standalone:true,
  imports:[CommonModule,FormsModule],
  template:`<div class="page-head"><div><h2>Riesgo académico</h2><p>Alertas generadas cuando el promedio de un curso cae bajo el umbral configurado.</p></div><button class="btn btn-outline-secondary" (click)="load()">Actualizar</button></div>
  <div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error">{{message}}</div>
  <div class="panel"><div class="form-grid mb-3"><input class="form-control search-input" [(ngModel)]="query" placeholder="Buscar estudiante, curso o periodo"><select class="form-select" [(ngModel)]="status"><option value="">Todos los estados</option><option value="PENDIENTE">Pendiente</option><option value="ATENDIDA">Atendida</option><option value="RESUELTA">Resuelta</option></select></div>
  <div class="table-responsive"><table class="table table-hover"><thead><tr><th>Generada</th><th>Estudiante</th><th>Curso / periodo</th><th>Promedio</th><th>Motivo</th><th>Estado</th><th *ngIf="auth.role==='ADMINISTRADOR'">Acción</th></tr></thead><tbody>
  <tr *ngFor="let alert of filtered"><td>{{alert.fechaGeneracion|date:'short'}}</td><td>{{alert.estudiante}}</td><td>{{alert.curso}} · {{alert.periodo}}</td><td><span class="grade-pill low">{{alert.promedio}} / 20</span><small class="d-block">Umbral: {{alert.umbral}}</small></td><td>{{alert.motivo}}</td><td>{{alert.estado}}</td><td *ngIf="auth.role==='ADMINISTRADOR'"><select class="form-select form-select-sm" [ngModel]="alert.estado" (ngModelChange)="changeStatus(alert,$event)"><option value="PENDIENTE">Pendiente</option><option value="ATENDIDA">Atendida</option><option value="RESUELTA">Resuelta</option></select></td></tr>
  <tr *ngIf="!filtered.length"><td [attr.colspan]="auth.role==='ADMINISTRADOR'?7:6" class="text-center text-muted">No hay alertas para mostrar.</td></tr></tbody></table></div></div>`
})
export class AcademicAlertsComponent implements OnInit {
  private api=inject(ApiService);
  auth=inject(AuthService);
  items:any[]=[];
  query='';
  status='';
  message='';
  error=false;

  ngOnInit(){this.load();}
  load(){this.api.academicAlerts().subscribe({next:items=>this.items=items,error:e=>this.fail(e)});}
  get filtered(){const q=(this.query||'').trim().toLowerCase();return this.items.filter(x=>
    (!q||`${x.estudiante} ${x.curso} ${x.periodo}`.toLowerCase().includes(q))&&(!this.status||x.estado===this.status));}
  changeStatus(alert:any,status:string){
    this.api.updateAcademicAlertStatus(alert.id,status).subscribe({
      next:updated=>{Object.assign(alert,updated);this.message='Estado actualizado.';this.error=false;},
      error:e=>this.fail(e)
    });
  }
  private fail(e:any){this.message=e?.error?.message||'No se pudieron consultar las alertas.';this.error=true;}
}
