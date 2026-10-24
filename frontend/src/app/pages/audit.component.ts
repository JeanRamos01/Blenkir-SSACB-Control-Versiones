import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({selector:'app-audit',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Bitácora de auditoría</h2><p>Las últimas 200 operaciones registradas en los nuevos módulos administrativos.</p></div><button class="btn btn-outline-secondary" (click)="load()"><i class="bi bi-arrow-clockwise"></i> Actualizar</button></div>
<div class="panel"><input class="form-control search-input mb-3" [(ngModel)]="query" placeholder="Filtrar por usuario, acción o módulo"><div class="table-responsive"><table class="table table-hover"><thead><tr><th>Fecha</th><th>Usuario</th><th>Acción</th><th>Entidad</th><th>Referencia</th></tr></thead><tbody><tr *ngFor="let x of filtered"><td>{{x.fecha|date:'short'}}</td><td>{{x.usuario}}</td><td>{{x.accion}}</td><td>{{x.entidad}}</td><td>{{x.referencia}}</td></tr><tr *ngIf="!filtered.length"><td colspan="5" class="text-muted text-center">No hay movimientos en el filtro.</td></tr></tbody></table></div></div>`})
export class AuditComponent implements OnInit {
  private api=inject(ApiService);items:any[]=[];query='';ngOnInit(){this.load();}
  load(){this.api.audit().subscribe(x=>this.items=x);}
  get filtered(){const q=this.query.toLowerCase();return this.items.filter(x=>(x.usuario+' '+x.accion+' '+x.entidad).toLowerCase().includes(q));}
}
