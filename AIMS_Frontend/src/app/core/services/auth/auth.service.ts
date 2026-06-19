import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { LoginRequest } from '../../../features/auth/models/login-request.model';
import { LoginResponse } from '../../../features/auth/models/login-response.model';
import { environment } from '../../../../environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly BASE_URL = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(
      request: LoginRequest
  ): Observable<LoginResponse> {

      return this.http.post<LoginResponse>(
          `${this.BASE_URL}/login`,
          request
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('username');
  }

  getToken(): string | null {

      return localStorage.getItem('token');
  }

  getRole(): string | null {

      return localStorage.getItem('role');
  }

  isLoggedIn(): boolean {

      return !!this.getToken();
  }
}