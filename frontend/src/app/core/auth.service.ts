import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Session { token:string; username:string; displayName:string; role:string; }
@Injectable({providedIn:'root'})
export class AuthService{
  private http=inject(HttpClient);
  private key='ssacb_session';
  private subject=new BehaviorSubject<Session|null>(this.read());
  session$=this.subject.asObservable();
  get session(){return this.subject.value;}
  get role(){return this.session?.role || '';}
  login(username:string,password:string){return this.http.post<Session>(`${environment.apiBaseUrl}/auth/login`,{username,password}).pipe(tap(s=>{localStorage.setItem(this.key,JSON.stringify(s));this.subject.next(s);}));}
  logout(){localStorage.removeItem(this.key);this.subject.next(null);}
  private read():Session|null{try{return JSON.parse(localStorage.getItem(this.key)||'null');}catch{return null;}}
}
