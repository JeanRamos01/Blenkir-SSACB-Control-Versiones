import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({selector:'app-cash',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Caja y movimientos</h2><p>Registro contable acumulado de cobros e ingresos o egresos manuales.</p></div><span class="period-chip">Soles peruanos (PEN)</span></div>
<div class="metric-grid" *ngIf="data"><div class="metric-card green"><i class="bi bi-arrow-down-circle"></i><div><span>Ingresos acumulados</span><strong>S/ {{data.ingresos|number:'1.2-2'}}</strong></div></div>
<div class="metric-card orange"><i class="bi bi-arrow-up-circle"></i><div><span>Egresos acumulados</span><strong>S/ {{data.egresos|number:'1.2-2'}}</strong></div></div>
<div class="metric-card blue"><i class="bi bi-wallet2"></i><div><span>Saldo registrado</span><strong>S/ {{data.saldo|number:'1.2-2'}}</strong></div></div></div>
<section class="panel form-panel" *ngIf="auth.role==='ADMINISTRADOR'"><h3>Movimiento manual</h3><div class="form-grid"><select class="form-select" [(ngModel)]="form.tipo"><option value="INGRESO">Ingreso</option><option value="EGRESO">Egreso</option></select><input class="form-control" placeholder="Motivo / concepto" [(ngModel)]="form.concepto"><input class="form-control" type="number" min="0.01" step="0.01" placeholder="Importe S/" [(ngModel)]="form.importe"><button class="btn btn-orange" [disabled]="!form.concepto||!(form.importe>0)" (click)="save()">Registrar movimiento</button></div></section>
<div *ngIf="message" class="alert alert-danger">{{message}}</div>
<section class="panel" *ngIf="data"><h3>Libro de caja</h3><div class="table-responsive"><table class="table table-hover"><thead><tr><th>Fecha / hora</th><th>Tipo</th><th>Concepto</th><th>Importe</th><th>Responsable</th><th>Origen</th></tr></thead><tbody>
<tr *ngFor="let x of data.movimientos"><td>{{x.fecha|date:'short'}}</td><td><span class="status" [class.ok]="x.tipo==='INGRESO'" [class.bad]="x.tipo==='EGRESO'">{{x.tipo}}</span></td><td>{{x.concepto}}</td><td>S/ {{x.importe|number:'1.2-2'}}</td><td>{{x.usuario}}</td><td>{{x.pagoId?'Pago #'+x.pagoId:'Manual'}}</td></tr>
<tr *ngIf="!data.movimientos.length"><td colspan="6" class="text-muted text-center">Aún no se registraron movimientos.</td></tr></tbody></table></div></section>`})
export class CashComponent implements OnInit {
  private api=inject(ApiService);auth=inject(AuthService);data:any;form:any={tipo:'EGRESO',concepto:'',importe:null};message='';
  ngOnInit(){this.load();}load(){this.api.cash().subscribe({next:x=>this.data=x,error:e=>this.message=e?.error?.message||'No se pudo consultar la caja.'});}
  save(){this.api.manualCash(this.form).subscribe({next:()=>{this.form={tipo:'EGRESO',concepto:'',importe:null};this.message='';this.load();},error:e=>this.message=e?.error?.message||'No se pudo registrar.'});}
}
