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
  transactionId = '';
  customerName = '';
  phoneNumber = '';
  shippingAddress = '';
  totalAmount = 0;
  transactionDatetime = '';
  isLoading = true;

  constructor(
    private readonly router: Router,
    private readonly vietQRPaymentService: VietQRPaymentService,
  ) { }

  ngOnInit(): void {
    const state = history.state;
    const orderId: string = state?.orderId || 'ORD-001';
    this.transactionId = state?.transactionId || '';

    // Only call confirmation API if we arrived here via polling success
    // (transactionId is set). If the user navigated manually or via confirmPayment()
    // without a transactionId, skip the API call and show fallback immediately.
    if (!this.transactionId) {
      this.orderReference = `AIMS-${orderId}-SUC`;
      this.isLoading = false;
      return;
    }

    // Fetch real confirmation from backend
    this.vietQRPaymentService.getOrderConfirmation(orderId).subscribe({
      next: (confirmation) => {
        this.customerName = confirmation.customerName || '';
        this.phoneNumber = confirmation.phoneNumber || '';
        this.shippingAddress = [confirmation.shippingAddress, confirmation.province]
          .filter(Boolean).join(', ');
        this.totalAmount = confirmation.totalAmountToBePaid || 0;
        this.transactionId = confirmation.transactionId || this.transactionId;
        this.transactionDatetime = confirmation.transactionDatetimeDisplay || '';
        this.orderReference = `AIMS-${orderId}-SUC`;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load confirmation:', err);
        // Fallback: show what we have from navigation state
        this.orderReference = `AIMS-${orderId}-SUC`;
        this.isLoading = false;
      }
    });
  }

  backToShop(): void {
    this.router.navigate(['/']);
  }

  formatCurrency(amount: number): string {
    return `${amount.toLocaleString('vi-VN')} VND`;
  }
}