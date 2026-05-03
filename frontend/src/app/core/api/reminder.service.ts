import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReminderInfoResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ReminderService {
  private readonly http = inject(HttpClient);

  getInfo(): Observable<ReminderInfoResponse> {
    return this.http.get<ReminderInfoResponse>(`${environment.apiUrl}/api/v1/reminders/info`);
  }

  sendNow(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}/api/v1/reminders/send-now`, {});
  }
}

