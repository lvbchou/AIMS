// ============================================================
// FILE: src/app/features/payment/pages/payment-validating/payment-validating.component.ts
// ============================================================

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PaymentMockService } from '../../services/payment-mock.service';

@Component({
  selector: 'app-payment-validating',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-validating.component.html',
  styleUrls: ['./payment-validating.component.scss'],
})
export class PaymentValidatingComponent implements OnInit {

  step = 0;

  constructor(
    private readonly router: Router,
    private readonly paymentMockService: PaymentMockService,
  ) { }

  ngOnInit(): void {
    const state = history.state;
    const transactionId: string = state?.transactionId ?? `MOCK-${Date.now()}`;

    // Update step periodically for visual effect
    setTimeout(() => this.step = 1, 1000);
    setTimeout(() => this.step = 2, 2000);

    // Gọi service xác thực → điều hướng theo kết quả
    (this.paymentMockService as any).validatePayment(transactionId).subscribe((result: any) => {
      if (result.success) {
        this.router.navigate(['/payment/success'], {
          state: {
            orderReference: result.orderReference,
            transactionId: result.transactionId,
          },
        });
      } else {
        this.router.navigate(['/payment/failed'], {
          state: {
            errorMessage: result.errorMessage,
            errorCode: result.errorCode,
            transactionId: result.transactionId,
          },
        });
      }
    });
  }
}