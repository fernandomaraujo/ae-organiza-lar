import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ReminderService } from '../../core/api/reminder.service';
import { ReminderInfoResponse } from '../../core/models/api.models';

@Component({
  selector: 'app-reminders-page',
  templateUrl: './reminders-page.component.html',
  styleUrl: './reminders-page.component.scss'
})
export class RemindersPageComponent {
  private readonly reminderService = inject(ReminderService);

  readonly loading = signal(true);
  readonly info = signal<ReminderInfoResponse | null>(null);
  readonly message = signal('');
  readonly error = signal('');
  readonly sending = signal(false);

  constructor() {
    this.loadInfo();
  }

  sendNow(): void {
    if (this.sending()) {
      return;
    }

    this.sending.set(true);
    this.message.set('');
    this.error.set('');

    this.reminderService.sendNow().subscribe({
      next: (response) => {
        this.message.set(response.message);
        this.sending.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(error.error?.message ?? 'Não foi possível enviar lembrete agora.');
        this.sending.set(false);
      }
    });
  }

  private loadInfo(): void {
    this.reminderService.getInfo().subscribe({
      next: (response) => {
        this.info.set(response);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(error.error?.message ?? 'Não foi possível carregar informações de lembrete.');
        this.loading.set(false);
      }
    });
  }
}

