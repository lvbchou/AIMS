// ============================================================
// FILE: src/app/features/payment/pages/payment-failed/payment-failed.component.ts
// ============================================================

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-payment-failed',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-failed.component.html',
  styleUrls: ['./payment-failed.component.scss'],
})
export class PaymentFailedComponent implements OnInit {

  errorMessage = 'Unfortunately, we are unable to process your payment.';
  errorCode = '';
  orderId = '';
  orderReference = '';

  constructor(private readonly router: Router) { }

  ngOnInit(): void {
    const state = history.state;
    if (state?.errorMessage) this.errorMessage = state.errorMessage;
    if (state?.reason) this.errorMessage = state.reason;
    if (state?.errorCode) this.errorCode = state.errorCode;

    this.orderId = state?.orderId || '';

    if (this.orderId) {
      // Build short friendly reference from orderId
      const shortId = this.orderId.replace(/-/g, '').slice(-8).toUpperCase();
      this.orderReference = `#AIMS-${shortId}`;
    } else {
      this.orderReference = this.errorCode || '#AIMS-ERR';
    }
  }

  retryPayment(): void {
    // Navigate back to VietQR screen with same orderId if available
    if (this.orderId) {
      this.router.navigate(['/payment/vietqr'], { queryParams: { orderId: this.orderId } });
    } else {
      this.router.navigate(['/payment/vietqr']);
    }
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}