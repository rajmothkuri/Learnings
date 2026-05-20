import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Payment</h2>
    <p>Payment microservice UI placeholder.</p>
  `
})
export class PaymentComponent {}
