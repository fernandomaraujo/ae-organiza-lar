import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { UserResponse } from '../../core/models/api.models';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss'
})
export class HomePageComponent {
  private readonly authService = inject(AuthService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly user = signal<UserResponse | null>(null);

  constructor() {
    this.authService.me().subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar os dados do usuário.');
        this.loading.set(false);
      }
    });
  }
}

