export interface ErrorResponse {
  message: string;
  status: number;
}

export interface LoginRequest {
  email: string;
}

export interface VerifyCodeRequest {
  email: string;
  code: string;
}

export interface LoginResponse {
  message: string;
  email: string;
}

export interface UserResponse {
  id: string;
  email: string;
  name: string | null;
  createdAt: string;
  lastLogin: string | null;
  preferences: unknown;
}

export interface VerifyCodeResponse {
  token: string;
  tokenType: 'Bearer';
  message: string;
  user: UserResponse;
}

export interface ReminderInfoResponse {
  schedule: string;
  description: string;
  manualSendEndpoint: string;
}

export interface HomeRequest {
  name: string;
  description: string | null;
}

export interface HomeResponse {
  id: string;
  userId: string;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface TaskScheduleResponse {
  id: string;
  dayOfWeek: number;
  priority: TaskPriority;
  notes: string | null;
  createdAt: string;
}

export interface TaskRequest {
  title: string;
  description: string | null;
  dayOfWeek: number;
  priority: TaskPriority;
  notes: string | null;
}

export interface TaskResponse {
  id: string;
  homeId: string;
  title: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
  schedule: TaskScheduleResponse;
}

export interface TaskCompletionRequest {
  date: string;
  notes: string | null;
}

export interface TaskCompletionResponse {
  id: string;
  taskId: string;
  completionDate: string;
  completedAt: string;
  notes: string | null;
}

export interface WeeklyPlanTaskResponse {
  taskId: string;
  title: string;
  description: string | null;
  priority: TaskPriority;
  notes: string | null;
  completed: boolean;
  completion: TaskCompletionResponse | null;
}

export interface WeeklyPlanDayResponse {
  dayOfWeek: number;
  dayName: string;
  date: string;
  tasks: WeeklyPlanTaskResponse[];
}

export interface WeeklyPlanResponse {
  homeId: string;
  homeName: string;
  weekStart: string;
  weekEnd: string;
  days: WeeklyPlanDayResponse[];
}

