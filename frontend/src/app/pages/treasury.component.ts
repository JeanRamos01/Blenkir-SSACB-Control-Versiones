import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({selector:'app-treasury',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="page-head"><div><h2>Tesorería escolar</h2><p>Conceptos, cuentas por cobrar y pagos. Los abonos actualizan automáticamente la caja.</p></div><span class="period-chip">Moneda PEN · S/</span></div>
<div *ngIf="message" class="alert" [class.alert-danger]="error" [class.alert-success]="!error" role="status">{{message}}</div>
<div class="split form-panel">
 <section class="panel" *ngIf="auth.role==='ADMINISTRADOR'"><h3>Crear concepto de cobro</h3><div class="form-grid two"><input class="form-control" placeholder="Ej.: Pensión mensual" [(ngModel)]="concept.nombre"><input class="form-control" type="number" min="0.01" step="0.01" placeholder="Importe S/" [(ngModel)]="concept.importe"></div><button class="btn btn-orange mt-3" [disabled]="!concept.nombre||!(concept.importe>0)" (click)="createConcept()">Guardar concepto</button></section>
 <section class="panel"><h3>Generar cuenta por cobrar</h3><div class="form-grid two">
  <select class="form-select" [(ngModel)]="charge.estudianteId"><option [ngValue]="null">Estudiante</option><option *ngFor="let s of students" [ngValue]="s.id">{{s.nombres}} {{s.apellidos}}</option></select>
  <select class="form-select" [(ngModel)]="charge.conceptoId"><option [ngValue]="null">Concepto</option><option *ngFor="let c of concepts" [ngValue]="c.id">{{c.nombre}} · S/ {{c.importe|number:'1.2-2'}}</option></select>
  <input class="form-control" placeholder="Periodo: 2026-10" [(ngModel)]="charge.periodo"><input class="form-control" type="date" [(ngModel)]="charge.vencimiento" aria-label="Vencimiento"></div>
  <button class="btn btn-orange mt-3" [disabled]="!charge.estudianteId||!charge.conceptoId||!charge.periodo||!charge.vencimiento" (click)="createCharge()">Generar cuenta</button></section>
</div>
<section class="panel form-panel"><h3>Registrar abono</h3><div class="form-grid">
  <select class="form-select" [(ngModel)]="payment.cuentaId"><option [ngValue]="null">Cuenta pendiente</option><option *ngFor="let x of due" [ngValue]="x.id">#{{x.id}} · {{x.estudiante}} · {{x.concepto}} · S/ {{x.saldo|number:'1.2-2'}}</option></select>
  <input class="form-control" type="number" min="0.01" step="0.01" placeholder="Importe del abono" [(ngModel)]="payment.importe">
  <select class="form-select" [(ngModel)]="payment.medio"><option *ngFor="let m of media" [value]="m">{{m}}</option></select>
  <input class="form-control" placeholder="Referencia de operación (opcional)" [(ngModel)]="payment.referencia"><button class="btn btn-primary" [disabled]="!payment.cuentaId||!(payment.importe>0)" (click)="pay()">Registrar pago</button></div>
</section>
<section class="panel"><div class="d-flex justify-content-between align-items-center mb-3"><h3 class="mb-0">Estado de cuentas ({{filtered.length}})</h3><input class="form-control search-input" placeholder="Buscar estudiante o concepto" [(ngModel)]="query"></div><div class="table-responsive"><table class="table table-hover"><thead><tr><th>Estudiante</th><th>Concepto</th><th>Periodo</th><th>Vence</th><th>Importe</th><th>Pagado</th><th>Saldo</th><th>Estado</th></tr></thead><tbody>
<tr *ngFor="let x of filtered"><td>{{x.estudiante}}</td><td>{{x.concepto}}</td><td>{{x.periodo}}</td><td>{{x.vencimiento}}</td><td>S/ {{x.importe|number:'1.2-2'}}</td><td>S/ {{x.pagado|number:'1.2-2'}}</td><td class="fw-semibold">S/ {{x.saldo|number:'1.2-2'}}</td><td><span class="status" [class.ok]="x.estado==='PAGADO'" [class.warn]="x.estado==='PARCIAL'||x.estado==='PENDIENTE'" [class.bad]="x.estado==='VENCIDO'">{{x.estado}}</span></td></tr>
<tr *ngIf="!filtered.length"><td colspan="8" class="text-muted text-center">Todavía no hay cuentas.</td></tr></tbody></table></div></section>`})
export class TreasuryComponent implements OnInit {
  private api=inject(ApiService);auth=inject(AuthService);students:any[]=[];concepts:any[]=[];charges:any[]=[];
  concept:any={nombre:'',importe:null};charge:any={estudianteId:null,conceptoId:null,periodo:'2026-10',vencimiento:''};
  payment:any={cuentaId:null,importe:null,medio:'EFECTIVO',referencia:''};media=['EFECTIVO','TRANSFERENCIA','TARJETA','YAPE','PLIN'];query='';message='';error=false;
  ngOnInit(){this.load();this.api.students().subscribe(x=>this.students=x);}
  load(){this.api.concepts().subscribe(x=>this.concepts=x);this.api.charges().subscribe(x=>this.charges=x);}
  get due(){return this.charges.filter(c=>Number(c.saldo)>0);}
  get filtered(){const q=this.query.toLowerCase();return this.charges.filter(x=>(x.estudiante+' '+x.concepto).toLowerCase().includes(q));}
  createConcept(){this.api.createConcept(this.concept).subscribe({next:()=>{this.concept={nombre:'',importe:null};this.success('Concepto guardado.');this.load();},error:e=>this.failure(e)});}
  createCharge(){this.api.createCharge(this.charge).subscribe({next:()=>{this.success('Cuenta generada.');this.load();},error:e=>this.failure(e)});}
  pay(){this.api.recordPayment(this.payment).subscribe({next:()=>{this.payment={cuentaId:null,importe:null,medio:'EFECTIVO',referencia:''};this.success('Pago registrado en Tesorería y Caja.');this.load();},error:e=>this.failure(e)});}
  success(m:string){this.message=m;this.error=false;}failure(e:any){this.message=e?.error?.message||'Operación no completada.';this.error=true;}
}
