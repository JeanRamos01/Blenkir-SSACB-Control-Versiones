import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
export const authGuard:CanActivateFn=()=>{const a=inject(AuthService),r=inject(Router);return a.session?true:r.createUrlTree(['/login']);};
export const roleGuard:CanActivateFn=(route)=>{const a=inject(AuthService),r=inject(Router);const roles=(route.data?.['roles']||[]) as string[];return a.session && (!roles.length||roles.includes(a.role))?true:r.createUrlTree(['/']);};
