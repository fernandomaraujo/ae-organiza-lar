import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-request-code-page',
  imports: [ReactiveFormsModule],
  templateUrl: './request-code-page.component.html',
  styleUrl: './request-code-page.component.scss'
})
export class RequestCodePageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal('');
  readonly message = signal('');

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]]
  });

  submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.message.set('');

    const email = this.form.controls.email.value;

    this.authService.requestCode(email).subscribe({
      next: (response) => {
        this.message.set(response.message);
        this.loading.set(false);
        void this.router.navigate(['/verify-code'], { queryParams: { email } });
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(error.error?.message ?? 'Não foi possível enviar o código.');
        this.loading.set(false);
      }
    });
  }
}

