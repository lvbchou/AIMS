// ============================================================
// FILE: src/app/features/payment/pages/payment-failed/payment-failed.component.ts
// ============================================================

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../../order/services/order.service';

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

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly orderService: OrderService
  ) { }

  ngOnInit(): void {
    const state = history.state;
    const queryOrderId = this.route.snapshot.queryParamMap.get('orderId') || '';
    if (state?.errorMessage) this.errorMessage = state.errorMessage;
    if (state?.reason) this.errorMessage = state.reason;
    if (state?.errorCode) this.errorCode = state.errorCode;

    this.orderId = state?.orderId || queryOrderId || this.orderService.getCurrentOrderId() || '';
    if (this.orderId) {
      this.orderService.setCurrentOrderId(this.orderId);
    }

    if (this.orderId) {
      // Build short friendly reference from orderId
      const shortId = this.orderId.replace(/-/g, '').slice(-8).toUpperCase();
      this.orderReference = `#AIMS-${shortId}`;
    } else {
      this.orderReference = this.errorCode || '#AIMS-ERR';
    }
  }

  retryPayment(): void {
    if (this.orderId) {
      this.router.navigate(['/payment/vietqr'], { queryParams: { orderId: this.orderId } });
    } else {
      this.router.navigate(['/payment/vietqr']);
    }
  }

  changeDelivery(): void {
    if (this.orderId) {
      this.orderService.setCurrentOrderId(this.orderId);
      this.router.navigate(['/delivery'], { queryParams: { orderId: this.orderId } });
      return;
    }
    this.router.navigate(['/delivery']);
  }

  updateCart(): void {
    if (this.orderId) {
      this.orderService.setCurrentOrderId(this.orderId);
      this.router.navigate(['/cart'], { queryParams: { orderId: this.orderId } });
      return;
    }
    this.router.navigate(['/cart']);
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}
