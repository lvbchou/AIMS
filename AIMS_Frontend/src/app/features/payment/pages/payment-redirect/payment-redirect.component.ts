// ============================================================
// FILE: src/app/features/payment/pages/payment-redirect/payment-redirect.component.ts
// ============================================================

import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-payment-redirect',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-redirect.component.html',
  styleUrls: ['./payment-redirect.component.scss'],
})
export class PaymentRedirectComponent implements OnInit, OnDestroy {

  private orderId = 0;
  private totalPayable = 0;
  private provider = 'AIMS Store';
  private redirectTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(private readonly router: Router) { }

  ngOnInit(): void {
    const state = history.state;
    this.orderId      = state?.orderId      ?? 161;
    this.totalPayable = state?.totalPayable ?? 126_500;
    this.provider     = state?.provider     ?? 'AIMS Store';

    // Redirect thật đến PayPal sau 2.5 giây
    this.redirectTimeout = setTimeout(() => {
      window.location.href = 'https://www.paypal.com/signin';
    }, 2_500);
  }

  ngOnDestroy(): void {
    if (this.redirectTimeout) {
      clearTimeout(this.redirectTimeout);
    }
  }

  /** Huỷ redirect, quay về trang chọn phương thức thanh toán */
  cancel(): void {
    if (this.redirectTimeout) {
      clearTimeout(this.redirectTimeout);
      this.redirectTimeout = null;
    }
    this.router.navigate(['/payment']);
  }
}