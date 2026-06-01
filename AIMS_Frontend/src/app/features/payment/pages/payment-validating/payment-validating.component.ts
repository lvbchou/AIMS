// ============================================================
// FILE: src/app/features/payment/pages/payment-validating/payment-validating.component.ts
// ============================================================

import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { PaymentMockService } from '../../services/payment-mock.service';
import { VietQRPaymentService } from '../../services/vietqr-payment.service';
import { CartService } from '../../../cart/services/cart.service';
import { OrderService } from '../../../order/services/order.service';

@Component({
  selector: 'app-payment-validating',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-validating.component.html',
  styleUrls: ['./payment-validating.component.scss'],
})
export class PaymentValidatingComponent implements OnInit, OnDestroy {

  step = 0;
  isVietQR = false;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly paymentMockService: PaymentMockService,
    private readonly vietQRPaymentService: VietQRPaymentService,
    private readonly cartService: CartService,
    private readonly orderService: OrderService,
  ) { }

  ngOnInit(): void {
    const state = history.state;
    this.isVietQR = !!state?.isVietQR;
    const transactionId: string = state?.transactionId || '';
    const orderId: string = state?.orderId || '';

    console.log('[Validating] isVietQR:', this.isVietQR, '| transactionId:', transactionId, '| orderId:', orderId);

    // Advance step indicator
    setTimeout(() => this.step = 1, 1000);

    if (this.isVietQR) {
      if (!orderId) {
        console.error('[Validating] No orderId in state — cannot poll');
        this.router.navigate(['/payment/failed'], {
          state: { reason: 'Missing order information' }
        });
        return;
      }

      // Poll by orderId — works even if transactionId was not captured
      this.vietQRPaymentService.pollByOrderId(orderId).subscribe({
        next: (status) => {
          console.log('[Validating] Polling resolved COMPLETED:', status);
          this.step = 2;
          setTimeout(() => {
            this.cartService.clear();
            this.orderService.clearCheckoutState();
            this.router.navigate(['/payment/success'], {
              state: {
                orderId,
                transactionId: status.transactionId || transactionId,
              },
            });
          }, 1000);
        },
        error: (err) => {
          console.error('[Validating] Polling error:', err);
          this.router.navigate(['/payment/failed'], {
            state: {
              orderId,
              reason: err?.message || 'Payment failed or timed out',
            },
          });
        }
      });
    } else {
      // Original Mock validation for PayPal / COD
      setTimeout(() => this.step = 2, 2000);

      (this.paymentMockService as any).validatePayment(transactionId).subscribe((result: any) => {
        if (result.success) {
          this.cartService.clear();
          this.orderService.clearCheckoutState();
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

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}