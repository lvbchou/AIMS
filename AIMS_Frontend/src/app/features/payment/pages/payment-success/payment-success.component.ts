// ============================================================
// FILE: src/app/features/payment/pages/payment-success/payment-success.component.ts
// ============================================================

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { VietQRPaymentService } from '../../services/vietqr-payment.service';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-success.component.html',
  styleUrls: ['./payment-success.component.scss'],
})
export class PaymentSuccessComponent implements OnInit {

  orderReference = '';
  isLoading = false; // start as false — show content immediately

  private orderId = '';

  constructor(
    private readonly router: Router,
    private readonly vietQRPaymentService: VietQRPaymentService,
  ) { }

  ngOnInit(): void {
    const state = history.state;
    this.orderId = state?.orderId || '';
    const transactionId: string = state?.transactionId || '';

    console.log('[Success] orderId:', this.orderId, '| transactionId:', transactionId);

    // Hiển thị fallback ngay lập tức — không chờ API
    if (this.orderId) {
      const shortId = this.orderId.replace(/-/g, '').slice(-8).toUpperCase();
      this.orderReference = `Order #${shortId}`;
    } else {
      this.orderReference = transactionId || 'N/A';
    }

    // Nếu có orderId, thử gọi API để lấy tên sản phẩm thật (non-blocking)
    if (this.orderId) {
      this.vietQRPaymentService.getOrderConfirmation(this.orderId).subscribe({
        next: (confirmation) => {
          if (confirmation?.orderName) {
            this.orderReference = confirmation.orderName;
          }
        },
        error: () => { /* keep fallback */ }
      });
    }
  }

  backToShop(): void {
    this.router.navigate(['/']);
  }
}