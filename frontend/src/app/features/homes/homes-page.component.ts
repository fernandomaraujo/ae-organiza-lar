import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { HomeService } from '../../core/api/home.service';
import { HomeRequest, HomeResponse } from '../../core/models/api.models';

@Component({
  selector: 'app-homes-page',
  imports: [ReactiveFormsModule],
  templateUrl: './homes-page.component.html',
  styleUrl: './homes-page.component.scss'
})
export class HomesPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly homeService = inject(HomeService);

  readonly homes = signal<HomeResponse[]>([]);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly deletingId = signal<string | null>(null);
  readonly editingId = signal<string | null>(null);
  readonly error = signal('');
  readonly success = signal('');

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
    description: ['', [Validators.maxLength(500)]]
  });

  constructor() {
    this.loadHomes();
  }

  get isEditing(): boolean {
    return this.editingId() !== null;
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set('');
    this.success.set('');

    const payload = this.toPayload();
    const request$ = this.isEditing
      ? this.homeService.updateHome(this.editingId()!, payload)
      : this.homeService.createHome(payload);

    request$
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (home) => {
          if (this.isEditing) {
            this.homes.update((homes) => homes.map((item) => (item.id === home.id ? home : item)));
            this.success.set('Lar atualizado com sucesso.');
          } else {
            this.homes.update((homes) => [home, ...homes]);
            this.success.set('Lar criado com sucesso.');
          }

          this.resetForm();
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.extractErrorMessage(error, 'Não foi possível salvar o lar.'));
        }
      });
  }

  startEdit(home: HomeResponse): void {
    this.editingId.set(home.id);
    this.error.set('');
    this.success.set('');
    this.form.setValue({
      name: home.name,
      description: home.description ?? ''
    });
  }

  cancelEdit(): void {
    this.resetForm();
    this.error.set('');
    this.success.set('Edição cancelada.');
  }

  deleteHome(home: HomeResponse): void {
    if (this.deletingId() || !window.confirm(`Deseja excluir o lar "${home.name}"? As tarefas ligadas a ele também serão removidas.`)) {
      return;
    }

    this.deletingId.set(home.id);
    this.error.set('');
    this.success.set('');

    this.homeService
      .deleteHome(home.id)
      .pipe(finalize(() => this.deletingId.set(null)))
      .subscribe({
        next: () => {
          this.homes.update((homes) => homes.filter((item) => item.id !== home.id));

          if (this.editingId() === home.id) {
            this.resetForm();
          }

          this.success.set('Lar excluído com sucesso.');
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.extractErrorMessage(error, 'Não foi possível excluir o lar.'));
        }
      });
  }

  trackByHomeId(_: number, home: HomeResponse): string {
    return home.id;
  }

  private loadHomes(): void {
    this.loading.set(true);
    this.error.set('');

    this.homeService
      .listHomes()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (homes) => {
          const sortedHomes = [...homes].sort((a, b) => b.createdAt.localeCompare(a.createdAt));
          this.homes.set(sortedHomes);
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.extractErrorMessage(error, 'Não foi possível carregar seus lares.'));
        }
      });
  }

  private resetForm(): void {
    this.form.reset({
      name: '',
      description: ''
    });
    this.editingId.set(null);
  }

  private toPayload(): HomeRequest {
    const { name, description } = this.form.getRawValue();
    const normalizedDescription = description.trim();

    return {
      name: name.trim(),
      description: normalizedDescription ? normalizedDescription : null
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
}

