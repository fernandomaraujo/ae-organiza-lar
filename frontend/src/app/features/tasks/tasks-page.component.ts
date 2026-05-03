import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, Observable } from 'rxjs';
import { HomeService } from '../../core/api/home.service';
import { TaskService } from '../../core/api/task.service';
import {
  HomeResponse,
  TaskCompletionResponse,
  TaskPriority,
  TaskRequest,
  TaskResponse,
  WeeklyPlanResponse
} from '../../core/models/api.models';

type TaskListItem = TaskResponse & {
  occurrenceDate: string | null;
  completed: boolean;
  completion: TaskCompletionResponse | null;
};

type DayTaskGroup = {
  dayOfWeek: number;
  dayName: string;
  date: string | null;
  tasks: TaskListItem[];
};

@Component({
  selector: 'app-tasks-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './tasks-page.component.html',
  styleUrl: './tasks-page.component.scss'
})
export class TasksPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly homeService = inject(HomeService);
  private readonly taskService = inject(TaskService);

  readonly homes = signal<HomeResponse[]>([]);
  readonly tasks = signal<TaskResponse[]>([]);
  readonly weeklyPlan = signal<WeeklyPlanResponse | null>(null);
  readonly homesLoading = signal(true);
  readonly tasksLoading = signal(false);
  readonly planLoading = signal(false);
  readonly submitting = signal(false);
  readonly deletingTaskId = signal<string | null>(null);
  readonly completionTaskId = signal<string | null>(null);
  readonly selectedHomeId = signal<string | null>(null);
  readonly selectedDay = signal<number | null>(null);
  readonly referenceDate = signal(this.todayIsoDate());
  readonly editingTaskId = signal<string | null>(null);
  readonly error = signal('');
  readonly success = signal('');

  readonly dayOptions = [
    { value: 1, label: 'Segunda-feira', shortLabel: 'Seg' },
    { value: 2, label: 'Terça-feira', shortLabel: 'Ter' },
    { value: 3, label: 'Quarta-feira', shortLabel: 'Qua' },
    { value: 4, label: 'Quinta-feira', shortLabel: 'Qui' },
    { value: 5, label: 'Sexta-feira', shortLabel: 'Sex' },
    { value: 6, label: 'Sábado', shortLabel: 'Sáb' },
    { value: 7, label: 'Domingo', shortLabel: 'Dom' }
  ] as const;

  readonly priorityOptions: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];

  readonly displayedTasks = computed<TaskListItem[]>(() => {
    const taskMeta = new Map<string, { occurrenceDate: string; completed: boolean; completion: TaskCompletionResponse | null }>();

    for (const day of this.weeklyPlan()?.days ?? []) {
      for (const task of day.tasks) {
        taskMeta.set(task.taskId, {
          occurrenceDate: day.date,
          completed: task.completed,
          completion: task.completion
        });
      }
    }

    return this.tasks().map((task) => {
      const metadata = taskMeta.get(task.id);

      return {
        ...task,
        occurrenceDate: metadata?.occurrenceDate ?? null,
        completed: metadata?.completed ?? false,
        completion: metadata?.completion ?? null
      };
    });
  });

  readonly groupedTasksByDay = computed<DayTaskGroup[]>(() => {
    const selectedDay = this.selectedDay();
    const displayedTasks = this.displayedTasks();
    const planDays = this.weeklyPlan()?.days ?? [];

    const tasksByDay = new Map<number, TaskListItem[]>();
    for (const task of displayedTasks) {
      const list = tasksByDay.get(task.schedule.dayOfWeek) ?? [];
      list.push(task);
      tasksByDay.set(task.schedule.dayOfWeek, list);
    }

    for (const list of tasksByDay.values()) {
      list.sort((a, b) => a.title.localeCompare(b.title));
    }

    if (planDays.length) {
      return planDays
        .filter((day) => selectedDay === null || day.dayOfWeek === selectedDay)
        .map((day) => ({
          dayOfWeek: day.dayOfWeek,
          dayName: day.dayName,
          date: day.date,
          tasks: tasksByDay.get(day.dayOfWeek) ?? []
        }));
    }

    const fallbackDays = this.dayOptions
      .filter((day) => selectedDay === null || day.value === selectedDay)
      .map((day) => ({
        dayOfWeek: day.value,
        dayName: day.label,
        date: null,
        tasks: tasksByDay.get(day.value) ?? []
      }));

    return fallbackDays;
  });

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
    description: ['', [Validators.maxLength(1000)]],
    dayOfWeek: [1, [Validators.required, Validators.min(1), Validators.max(7)]],
    priority: ['MEDIUM' as TaskPriority, [Validators.required]],
    notes: ['', [Validators.maxLength(500)]]
  });

  constructor() {
    this.loadHomes();
  }

  get isEditing(): boolean {
    return this.editingTaskId() !== null;
  }

  get selectedHome(): HomeResponse | undefined {
    return this.homes().find((home) => home.id === this.selectedHomeId());
  }

  onHomeChange(homeId: string): void {
    this.selectedHomeId.set(homeId || null);
    this.selectedDay.set(null);
    this.resetForm();
    this.success.set('');
    this.error.set('');

    if (homeId) {
      this.loadTasks(homeId);
      this.loadWeeklyPlan(homeId);
    } else {
      this.tasks.set([]);
      this.weeklyPlan.set(null);
    }
  }

  onDayFilterChange(day: number | null): void {
    this.selectedDay.set(day);
    this.resetForm();
    this.success.set('');
    this.error.set('');

    const homeId = this.selectedHomeId();
    if (homeId) {
      this.loadTasks(homeId);
      this.loadWeeklyPlan(homeId);
    }
  }

  onReferenceDateChange(date: string): void {
    this.referenceDate.set(date || this.todayIsoDate());
    this.success.set('');
    this.error.set('');

    const homeId = this.selectedHomeId();
    if (homeId) {
      this.loadWeeklyPlan(homeId);
    }
  }

  submit(): void {
    const homeId = this.selectedHomeId();

    if (!homeId) {
      this.error.set('Selecione um lar para cadastrar tarefas.');
      return;
    }

    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set('');
    this.success.set('');

    const payload = this.toPayload();
    const request$ = this.isEditing
      ? this.taskService.updateTask(homeId, this.editingTaskId()!, payload)
      : this.taskService.createTask(homeId, payload);

    request$
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.selectedDay.set(payload.dayOfWeek);
          this.loadTasks(homeId);
          this.loadWeeklyPlan(homeId);
          this.success.set(this.isEditing ? 'Tarefa atualizada com sucesso.' : 'Tarefa criada com sucesso.');
          this.resetForm();
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.extractErrorMessage(error, 'Não foi possível salvar a tarefa.'));
        }
      });
  }

  startEdit(task: TaskResponse): void {
    this.editingTaskId.set(task.id);
    this.error.set('');
    this.success.set('');
    this.form.setValue({
      title: task.title,
      description: task.description ?? '',
      dayOfWeek: task.schedule.dayOfWeek,
      priority: task.schedule.priority,
      notes: task.schedule.notes ?? ''
    });
  }

  cancelEdit(): void {
    this.resetForm();
    this.success.set('Edição cancelada.');
    this.error.set('');
  }

  deleteTask(task: TaskResponse): void {
    const homeId = this.selectedHomeId();

    if (!homeId) {
      return;
    }

    if (this.deletingTaskId() || !window.confirm(`Deseja excluir a tarefa "${task.title}"?`)) {
      return;
    }

    this.deletingTaskId.set(task.id);
    this.error.set('');
    this.success.set('');

    this.taskService
      .deleteTask(homeId, task.id)
      .pipe(finalize(() => this.deletingTaskId.set(null)))
      .subscribe({
        next: () => {
          this.loadTasks(homeId);
          this.loadWeeklyPlan(homeId);
          if (this.editingTaskId() === task.id) {
            this.resetForm();
          }
          this.success.set('Tarefa excluída com sucesso.');
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.extractErrorMessage(error, 'Não foi possível excluir a tarefa.'));
        }
      });
  }

  toggleCompletion(task: TaskListItem): void {
    const homeId = this.selectedHomeId();

    if (!homeId) {
      return;
    }

    if (!task.occurrenceDate) {
      this.error.set('Não foi possível identificar a data desta tarefa na semana selecionada.');
      return;
    }

    this.completionTaskId.set(task.id);
    this.error.set('');
    this.success.set('');

    const request$: Observable<unknown> = task.completed
      ? this.taskService.uncompleteTask(homeId, task.id, task.occurrenceDate)
      : this.taskService.completeTask(homeId, task.id, {
          date: task.occurrenceDate,
          notes: task.completion?.notes ?? null
        });

    request$
      .pipe(finalize(() => this.completionTaskId.set(null)))
      .subscribe({
        next: () => {
          this.loadWeeklyPlan(homeId);
          this.success.set(task.completed ? 'Tarefa desmarcada como concluída.' : 'Tarefa marcada como concluída.');
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.extractErrorMessage(error, 'Não foi possível atualizar a conclusão da tarefa.'));
        }
      });
  }

  trackByTaskId(_: number, task: TaskResponse): string {
    return task.id;
  }

  formatDay(dayOfWeek: number): string {
    return this.dayOptions.find((option) => option.value === dayOfWeek)?.label ?? `Dia ${dayOfWeek}`;
  }

  formatDate(dateValue: string): string {
    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
      return dateValue;
    }

    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(date);
  }

  formatWeekRange(): string {
    const plan = this.weeklyPlan();

    if (!plan) {
      return '';
    }

    return `${this.formatShortDate(plan.weekStart)} - ${this.formatShortDate(plan.weekEnd)}`;
  }

  formatShortDate(dateValue: string): string {
    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
      return dateValue;
    }

    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short'
    }).format(date);
  }

  priorityLabel(priority: TaskPriority): string {
    switch (priority) {
      case 'LOW':
        return 'Baixa';
      case 'HIGH':
        return 'Alta';
      default:
        return 'Média';
    }
  }

  private loadHomes(): void {
    this.homesLoading.set(true);
    this.error.set('');

    this.homeService
      .listHomes()
      .pipe(finalize(() => this.homesLoading.set(false)))
      .subscribe({
        next: (homes) => {
          const sortedHomes = [...homes].sort((a, b) => a.name.localeCompare(b.name));
          this.homes.set(sortedHomes);

          if (sortedHomes.length) {
            const firstHomeId = sortedHomes[0].id;
            this.selectedHomeId.set(firstHomeId);
            this.loadTasks(firstHomeId);
            this.loadWeeklyPlan(firstHomeId);
          }
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.extractErrorMessage(error, 'Não foi possível carregar os lares para tarefas.'));
        }
      });
  }

  private loadWeeklyPlan(homeId: string): void {
    this.planLoading.set(true);

    this.taskService
      .getWeeklyPlan(homeId, this.referenceDate())
      .pipe(finalize(() => this.planLoading.set(false)))
      .subscribe({
        next: (plan) => {
          this.weeklyPlan.set(plan);
        },
        error: (error: HttpErrorResponse) => {
          this.weeklyPlan.set(null);
          this.error.set(this.extractErrorMessage(error, 'Não foi possível carregar o planejamento semanal.'));
        }
      });
  }

  private loadTasks(homeId: string): void {
    this.tasksLoading.set(true);

    this.taskService
      .listTasks(homeId, this.selectedDay())
      .pipe(finalize(() => this.tasksLoading.set(false)))
      .subscribe({
        next: (tasks) => {
          const sortedTasks = [...tasks].sort((a, b) => {
            const dayComparison = a.schedule.dayOfWeek - b.schedule.dayOfWeek;
            if (dayComparison !== 0) {
              return dayComparison;
            }

            return a.title.localeCompare(b.title);
          });

          this.tasks.set(sortedTasks);
        },
        error: (error: HttpErrorResponse) => {
          this.tasks.set([]);
          this.error.set(this.extractErrorMessage(error, 'Não foi possível carregar as tarefas.'));
        }
      });
  }

  private resetForm(): void {
    this.form.reset({
      title: '',
      description: '',
      dayOfWeek: this.selectedDay() ?? 1,
      priority: 'MEDIUM',
      notes: ''
    });
    this.editingTaskId.set(null);
  }

  private toPayload(): TaskRequest {
    const { title, description, dayOfWeek, priority, notes } = this.form.getRawValue();
    const normalizedDescription = description.trim();
    const normalizedNotes = notes.trim();

    return {
      title: title.trim(),
      description: normalizedDescription ? normalizedDescription : null,
      dayOfWeek,
      priority,
      notes: normalizedNotes ? normalizedNotes : null
    };
  }

  private extractErrorMessage(error: HttpErrorResponse, fallback: string): string {
    const errorBody = error.error;

    if (typeof errorBody === 'string' && errorBody.trim()) {
      return errorBody;
    }

    if (errorBody?.message && typeof errorBody.message === 'string') {
      return errorBody.message;
    }

    return fallback;
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }
}

