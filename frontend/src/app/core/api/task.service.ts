import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  TaskCompletionRequest,
  TaskCompletionResponse,
  TaskRequest,
  TaskResponse,
  WeeklyPlanResponse
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);

  listTasks(homeId: string, dayOfWeek?: number | null): Observable<TaskResponse[]> {
    const params = dayOfWeek ? new HttpParams().set('dayOfWeek', dayOfWeek) : undefined;
    return this.http.get<TaskResponse[]>(`${environment.apiUrl}/api/v1/homes/${homeId}/tasks`, { params });
  }

  getWeeklyPlan(homeId: string, week: string): Observable<WeeklyPlanResponse> {
    const params = new HttpParams().set('week', week);
    return this.http.get<WeeklyPlanResponse>(`${environment.apiUrl}/api/v1/homes/${homeId}/weekly-plan`, { params });
  }

  createTask(homeId: string, payload: TaskRequest): Observable<TaskResponse> {
    return this.http.post<TaskResponse>(`${environment.apiUrl}/api/v1/homes/${homeId}/tasks`, payload);
  }

  updateTask(homeId: string, taskId: string, payload: TaskRequest): Observable<TaskResponse> {
    return this.http.put<TaskResponse>(`${environment.apiUrl}/api/v1/homes/${homeId}/tasks/${taskId}`, payload);
  }

  deleteTask(homeId: string, taskId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/api/v1/homes/${homeId}/tasks/${taskId}`);
  }

  completeTask(homeId: string, taskId: string, payload: TaskCompletionRequest): Observable<TaskCompletionResponse> {
    return this.http.post<TaskCompletionResponse>(
      `${environment.apiUrl}/api/v1/homes/${homeId}/tasks/${taskId}/completions`,
      payload
    );
  }

  uncompleteTask(homeId: string, taskId: string, date: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/api/v1/homes/${homeId}/tasks/${taskId}/completions/${date}`);
  }
}

