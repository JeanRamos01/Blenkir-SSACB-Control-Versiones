import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/role.guard';
import { LoginComponent } from './pages/login.component';
import { LayoutComponent } from './layout/layout.component';
import { DashboardComponent } from './pages/dashboard.component';
import { StudentsComponent } from './pages/students.component';
import { TeachersComponent } from './pages/teachers.component';
import { CoursesComponent } from './pages/courses.component';
import { EvaluationsComponent } from './pages/evaluations.component';
import { GradesComponent } from './pages/grades.component';
import { AttendanceComponent } from './pages/attendance.component';
import { GuardiansComponent } from './pages/guardians.component';
import { StudentPortalComponent } from './pages/student-portal.component';
import { GuardianPortalComponent } from './pages/guardian-portal.component';
import { ReportsComponent } from './pages/reports.component';
import { EnrollmentsComponent } from './pages/enrollments.component';
import { CurriculumComponent } from './pages/curriculum.component';
import { SchedulesComponent } from './pages/schedules.component';
import { StaffComponent } from './pages/staff.component';
import { TreasuryComponent } from './pages/treasury.component';
import { CashComponent } from './pages/cash.component';
import { NotificationsComponent } from './pages/notifications.component';
import { AuditComponent } from './pages/audit.component';
import { SiagieComponent } from './pages/siagie.component';
import { CompetenciesComponent } from './pages/competencies.component';
import { StudentRecordsComponent } from './pages/student-records.component';

export const routes:Routes=[
 {path:'login',component:LoginComponent},
 {path:'',component:LayoutComponent,canActivate:[authGuard],children:[
   {path:'dashboard',component:DashboardComponent},
   {path:'students',component:StudentsComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR']}},
   {path:'teachers',component:TeachersComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR']}},
   {path:'courses',component:CoursesComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','DOCENTE']}},
   {path:'evaluations',component:EvaluationsComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','DOCENTE']}},
   {path:'grades',component:GradesComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','DOCENTE']}},
   {path:'attendance',component:AttendanceComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','DOCENTE']}},
   {path:'guardians',component:GuardiansComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR']}},
   {path:'student',component:StudentPortalComponent,canActivate:[roleGuard],data:{roles:['ESTUDIANTE']}},
   {path:'guardian',component:GuardianPortalComponent,canActivate:[roleGuard],data:{roles:['APODERADO']}},
   {path:'reports',component:ReportsComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','DOCENTE']}},
   {path:'enrollments',component:EnrollmentsComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','SECRETARIA']}},
   {path:'curriculum',component:CurriculumComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR']}},
   {path:'schedules',component:SchedulesComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','SECRETARIA','DOCENTE','ESTUDIANTE']}},
   {path:'staff',component:StaffComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR']}},
   {path:'treasury',component:TreasuryComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','SECRETARIA']}},
   {path:'cash',component:CashComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','SECRETARIA']}},
   {path:'notifications',component:NotificationsComponent},
   {path:'audit',component:AuditComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR']}},
   {path:'siagie',component:SiagieComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','SECRETARIA']}},
   {path:'competencies',component:CompetenciesComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','DOCENTE']}},
   {path:'student-records',component:StudentRecordsComponent,canActivate:[roleGuard],data:{roles:['ADMINISTRADOR','DOCENTE']}},
   {path:'',pathMatch:'full',redirectTo:'dashboard'}
 ]},
 {path:'**',redirectTo:''}
];
