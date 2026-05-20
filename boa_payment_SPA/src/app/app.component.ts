import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  template: `
    <nav style="padding:10px;border-bottom:1px solid #ddd;">
      <a routerLink="/auth" style="margin-right:12px;">Auth</a>
      <a routerLink="/payment" style="margin-right:12px;">Payment</a>
      <a routerLink="/notification" style="margin-right:12px;">Notification</a>
      <a routerLink="/discovery" style="margin-right:12px;">Discovery</a>
      <a routerLink="/gateway" style="margin-right:12px;">Gateway</a>
    </nav>
    <main style="padding:16px;">
      <router-outlet></router-outlet>
    </main>
  `
})
export class AppComponent {}
