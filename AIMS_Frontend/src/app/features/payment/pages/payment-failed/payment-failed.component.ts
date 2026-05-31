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

  // Fallback mock values (hiển thị khi không có router state)
  errorMessage = 'Unfortunately, we are unable to process your payment.';
  errorCode = '#AIMS-82910-ERR';

  constructor(private readonly router: Router) { }

  ngOnInit(): void {
    const state = history.state;
    if (state?.errorMessage) this.errorMessage = state.errorMessage;
    if (state?.errorCode) this.errorCode = state.errorCode;
  }

  retryPayment(): void {
    // Quay lại màn hình thanh toán VietQR để thử lại
    this.router.navigate(['/payment/vietqr']);
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}