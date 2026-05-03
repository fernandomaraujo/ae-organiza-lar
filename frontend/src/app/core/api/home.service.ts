import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HomeRequest, HomeResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class HomeService {
  private readonly http = inject(HttpClient);

  listHomes(): Observable<HomeResponse[]> {
    return this.http.get<HomeResponse[]>(`${environment.apiUrl}/api/v1/homes`);
  }

  createHome(payload: HomeRequest): Observable<HomeResponse> {
    return this.http.post<HomeResponse>(`${environment.apiUrl}/api/v1/homes`, payload);
  }

  updateHome(homeId: string, payload: HomeRequest): Observable<HomeResponse> {
    return this.http.put<HomeResponse>(`${environment.apiUrl}/api/v1/homes/${homeId}`, payload);
  }

  deleteHome(homeId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/api/v1/homes/${homeId}`);
  }
}

