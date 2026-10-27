import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';
@Injectable({providedIn:'root'})
export class ApiService{
  private http=inject(HttpClient); private base=environment.apiBaseUrl;
  dashboard(){return this.http.get<any>(`${this.base}/dashboard`);}
  students(){return this.http.get<any[]>(`${this.base}/students`);}
  addStudent(x:any){return this.http.post(`${this.base}/students`,x);}
  teachers(){return this.http.get<any[]>(`${this.base}/teachers`);}
  addTeacher(x:any){return this.http.post(`${this.base}/teachers`,x);}
  courses(){return this.http.get<any[]>(`${this.base}/courses`);}
  addCourse(x:any){return this.http.post(`${this.base}/courses`,x);}
  courseStudents(id:number){return this.http.get<any[]>(`${this.base}/courses/${id}/students`);}
  enroll(courseId:number,studentId:number){return this.http.post(`${this.base}/courses/${courseId}/enroll/${studentId}`,{});}
  evaluations(courseId?:number){let p=new HttpParams(); if(courseId)p=p.set('courseId',courseId);return this.http.get<any[]>(`${this.base}/academic/evaluations`,{params:p});}
  addEvaluation(x:any){return this.http.post(`${this.base}/academic/evaluations`,x);}
  grades(courseId?:number){let p=new HttpParams(); if(courseId)p=p.set('courseId',courseId);return this.http.get<any[]>(`${this.base}/academic/grades`,{params:p});}
  saveGrade(x:any){return this.http.post(`${this.base}/academic/grades`,x);}
  attendance(courseId?:number){let p=new HttpParams(); if(courseId)p=p.set('courseId',courseId);return this.http.get<any[]>(`${this.base}/academic/attendance`,{params:p});}
  saveAttendance(x:any){return this.http.post(`${this.base}/academic/attendance`,x);}
  guardians(){return this.http.get<any[]>(`${this.base}/guardians`);}
  addGuardian(x:any){return this.http.post(`${this.base}/guardians`,x);}
  linkGuardian(guardianId:number,studentId:number,parentesco:string){return this.http.post(`${this.base}/guardians/${guardianId}/link/${studentId}`,{}, {params:{parentesco}});}
  myChildren(){return this.http.get<any[]>(`${this.base}/guardians/me/children`);}
  childSummary(dni:string){return this.http.get<any>(`${this.base}/guardians/me/children/dni/${dni}/summary`);}
  studentSummary(){return this.http.get<any>(`${this.base}/student/me/summary`);}
  enrollmentList(){return this.http.get<any[]>(`${this.base}/enrollments`);}
  registerEnrollment(studentId:number,courseId:number){return this.http.post<any>(`${this.base}/enrollments`,{},{params:{studentId,courseId}});}
  enrollmentStatus(id:number,active:boolean){return this.http.patch<any>(`${this.base}/enrollments/${id}/status`,{},{params:{active}});}
  curriculum(){return this.http.get<any[]>(`${this.base}/curriculum`);}
  assignTeacher(cursoId:number,docenteId:number|null){return this.http.put<any>(`${this.base}/curriculum/assignment`,{cursoId,docenteId});}
  schedules(){return this.http.get<any[]>(`${this.base}/schedules`);}
  addSchedule(x:any){return this.http.post<any>(`${this.base}/schedules`,x);}
  deleteSchedule(id:number){return this.http.delete(`${this.base}/schedules/${id}`);}
  staff(){return this.http.get<any[]>(`${this.base}/staff`);}
  saveStaff(x:any){return x.id?this.http.put<any>(`${this.base}/staff/${x.id}`,x):this.http.post<any>(`${this.base}/staff`,x);}
  secretariatUsers(){return this.http.get<any[]>(`${this.base}/secretariat-users`);}
  createSecretariatUser(x:any){return this.http.post<any>(`${this.base}/secretariat-users`,x);}
  secretariatStatus(id:number,active:boolean){return this.http.patch<any>(`${this.base}/secretariat-users/${id}/status`,{},{params:{active}});}
  concepts(){return this.http.get<any[]>(`${this.base}/finance/concepts`);}
  createConcept(x:any){return this.http.post<any>(`${this.base}/finance/concepts`,x);}
  charges(){return this.http.get<any[]>(`${this.base}/finance/charges`);}
  createCharge(x:any){return this.http.post<any>(`${this.base}/finance/charges`,x);}
  payments(){return this.http.get<any[]>(`${this.base}/finance/payments`);}
  recordPayment(x:any){return this.http.post<any>(`${this.base}/finance/payments`,x);}
  cash(){return this.http.get<any>(`${this.base}/finance/cash`);}
  manualCash(x:any){return this.http.post<any>(`${this.base}/finance/cash`,x);}
  notifications(){return this.http.get<any[]>(`${this.base}/notifications`);}
  postNotification(x:any){return this.http.post<any>(`${this.base}/notifications`,x);}
  audit(){return this.http.get<any[]>(`${this.base}/audit`);}
  competencies(){return this.http.get<any[]>(`${this.base}/competencies`);}
  createCompetency(x:any){return this.http.post<any>(`${this.base}/competencies`,x);}
  competencyResults(){return this.http.get<any[]>(`${this.base}/competencies/results`);}
  saveCompetencyResult(x:any){return this.http.post<any>(`${this.base}/competencies/results`,x);}
  downloadExport(path:'students'|'enrollments'|'competencies',format:'csv'|'xlsx'='csv'){
    return this.http.get(`${this.base}/exports/siagie/${path}.${format}`,{responseType:'blob'});
  }
}
