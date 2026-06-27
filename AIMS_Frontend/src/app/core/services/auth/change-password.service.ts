import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Dedicated HTTP service for the Change Password use case.
 *
 * Design rationale:
 *  - A new, independent service — auth.service.ts is NOT modified (OCP).
 *  - Single responsibility: only knows about the /api/auth/change-password endpoint.
 *  - Loose coupling: depends on HttpClient abstraction; callers depend on this
 *    service's public interface only.
 */
export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ChangePasswordApiResponse {
  success: boolean;
  message: string;
  data: null;
}

@Injectable({ providedIn: 'root' })
export class ChangePasswordService {

  private readonly endpoint = '/api/auth/change-password';

  constructor(private http: HttpClient) {}

  /**
   * Submits a change-password request.
   * The JWT token is automatically attached by the existing authInterceptor.
   * The backend extracts the username from the JWT — no username sent in body.
   */
  changePassword(payload: ChangePasswordPayload): Observable<ChangePasswordApiResponse> {
    return this.http.post<ChangePasswordApiResponse>(this.endpoint, payload);
  }
}
