import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginRequest,
  LoginResponse,
  UserResponse,
  VerifyCodeRequest,
  VerifyCodeResponse
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenKey = 'ae.auth.token';
  private readonly emailKey = 'ae.auth.pendingEmail';

  readonly isLoggedIn = signal<boolean>(this.hasToken());

  requestCode(email: string): Observable<LoginResponse> {
    const body: LoginRequest = { email };
    return this.http.post<LoginResponse>(`${environment.apiUrl}/api/v1/auth/request-code`, body).pipe(
      tap(() => localStorage.setItem(this.emailKey, email))
    );
  }

  verifyCode(email: string, code: string): Observable<VerifyCodeResponse> {
    const body: VerifyCodeRequest = { email, code };
    return this.http
      .post<VerifyCodeResponse>(`${environment.apiUrl}/api/v1/auth/verify-code`, body)
      .pipe(tap((response) => this.saveToken(response.token)));
  }

  me(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${environment.apiUrl}/api/v1/auth/me`);
  }

  getPendingEmail(): string {
    return localStorage.getItem(this.emailKey) ?? '';
  }

  clearPendingEmail(): void {
    localStorage.removeItem(this.emailKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.emailKey);
    this.isLoggedIn.set(false);
  }

  private saveToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
    this.isLoggedIn.set(true);
    this.clearPendingEmail();
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }
}
