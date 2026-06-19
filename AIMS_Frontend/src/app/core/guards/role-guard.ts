import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router
} from '@angular/router';

export const roleGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot
) => {

  const router = inject(Router);

  const roles = JSON.parse(
    localStorage.getItem('roles') || '[]'
  );

  const requiredRole = route.data['role'];

  if (!roles.includes(requiredRole)) {

    router.navigate(['/unauthorized']);

    return false;
  }

  return true;
};