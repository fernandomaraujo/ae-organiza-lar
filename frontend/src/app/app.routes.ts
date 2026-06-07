import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { RequestCodePageComponent } from './features/auth/pages/request-code-page.component';
import { VerifyCodePageComponent } from './features/auth/pages/verify-code-page.component';
import { HomePageComponent } from './features/home/home-page.component';
import { HomesPageComponent } from './features/homes/homes-page.component';
import { RemindersPageComponent } from './features/reminders/reminders-page.component';
import { TasksPageComponent } from './features/tasks/tasks-page.component';
import { AppShellComponent } from './layouts/app-shell/app-shell.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'login', component: RequestCodePageComponent },
  { path: 'verify-code', component: VerifyCodePageComponent },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'home', component: HomePageComponent },
      { path: 'homes', component: HomesPageComponent },
      { path: 'tasks', component: TasksPageComponent },
      { path: 'reminders', component: RemindersPageComponent }
    ]
  },
  { path: '**', redirectTo: 'home' }
];
