import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Notification</h2>
    <p>Notification microservice UI placeholder.</p>
  `
})
export class NotificationComponent {}
