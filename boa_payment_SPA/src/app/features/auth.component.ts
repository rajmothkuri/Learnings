import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Auth</h2>
    <p *ngIf="!isAuth()">You are not logged in.</p>
    <p *ngIf="isAuth()">You are logged in.</p>
    <button (click)="login()">Login</button>
    <button (click)="logout()">Logout</button>
  `
})
export class AuthComponent {
  constructor(private auth: AuthService) {}
  isAuth() {
    return this.auth.isAuthenticated();
  }
  login() { this.auth.login(); }
  logout() { this.auth.logout(); }
}
