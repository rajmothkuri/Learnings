import { Routes } from '@angular/router';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'auth', pathMatch: 'full' },
  {
    path: 'auth',
    loadComponent: () => import('./features/auth.component').then(m => m.AuthComponent)
  },
  {
    path: 'payment',
    loadComponent: () => import('./features/payment.component').then(m => m.PaymentComponent),
    canMatch: [authGuard]
  },
  {
    path: 'notification',
    loadComponent: () => import('./features/notification.component').then(m => m.NotificationComponent),
    canMatch: [authGuard]
  },
  {
    path: 'discovery',
    loadComponent: () => import('./features/discovery.component').then(m => m.DiscoveryComponent),
    canMatch: [authGuard]
  },
  {
    path: 'gateway',
    loadComponent: () => import('./features/gateway.component').then(m => m.GatewayComponent),
    canMatch: [authGuard]
  }
];
