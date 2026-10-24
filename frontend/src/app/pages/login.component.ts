import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({selector:'app-login',standalone:true,imports:[CommonModule,FormsModule],template:`
<div class="login-page">
  <div class="login-visual">
    <div class="visual-overlay"></div>
    <div class="visual-copy"><img src="assets/logo.png" alt="Blenkir"><h1>Sistema de Seguimiento Académico</h1><p>Información académica segura para administración, docentes, estudiantes y apoderados.</p></div>
  </div>
  <div class="login-panel">
    <form class="login-card" (ngSubmit)="submit()">
      <img class="login-logo" src="assets/logo.png" alt="Colegio Blenkir">
      <h2>Bienvenido al SSACB</h2><p>Ingresa con tus credenciales institucionales.</p>
      <label>Usuario</label><input class="form-control" [(ngModel)]="username" name="username" required autocomplete="username">
      <label>Contraseña</label><input class="form-control" [(ngModel)]="password" name="password" type="password" required autocomplete="current-password">
      <div class="alert alert-danger py-2" *ngIf="error">{{error}}</div>
      <button class="btn btn-primary w-100" [disabled]="loading">{{loading?'Ingresando...':'Ingresar'}}</button>
      <div class="demo-box"><b>Acceso institucional</b><span>Solicita credenciales al administrador del Colegio Blenkir.</span><span>Los usuarios de prueba se crean solo cuando el modo demostración está activado en el servidor.</span></div>
      <small class="security-note"><i class="bi bi-shield-lock"></i> El DNI identifica registros, pero nunca se usa como contraseña.</small>
    </form>
  </div>
</div>`})
export class LoginComponent{
 private auth=inject(AuthService);private router=inject(Router);
 username='';password='';error='';loading=false;
 submit(){this.error='';this.loading=true;this.auth.login(this.username,this.password).subscribe({next:()=>{this.loading=false;this.router.navigate(['/dashboard']);},error:()=>{this.loading=false;this.error='Usuario o contraseña incorrectos.';}});}
}
