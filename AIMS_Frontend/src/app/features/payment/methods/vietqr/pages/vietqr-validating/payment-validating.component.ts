import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { VietQRPaymentService } from '../../services/vietqr-payment.service';
import { CartService } from '../../../../../cart/services/cart.service';
import { OrderService } from '../../../../../order/services/order.service';

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
    private readonly vietQRPaymentService: VietQRPaymentService,
    private readonly cartService: CartService,
    private readonly orderService: OrderService,
  ) { }

  ngOnInit(): void {
    const state = history.state;
    this.isVietQR = !!state?.isVietQR;
    const transactionId: string = state?.transactionId || '';
    const orderId: string = state?.orderId || '';

    setTimeout(() => this.step = 1, 1000);

    if (this.isVietQR) {
      if (!orderId) {
        this.router.navigate(['/payment/failed'], {
          state: { reason: 'Missing order information' }
        });
        return;
      }

      this.vietQRPaymentService.triggerTestCallback(orderId).subscribe({
        next: (result) => {
          this.completeAndNavigate(orderId, result?.transactionId || transactionId);
        },
        error: (err) => {
          console.warn('[Validating] VietQR test callback failed, fallback to polling:', err);
          this.pollUntilCompleted(orderId, transactionId);
        }
      });
      return;
    }

    // Non-VietQR flow (e.g. PayPal) is handled by PaypalResultComponent
    this.router.navigate(['/payment/failed'], {
      state: { reason: 'Unsupported validation flow.' }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private pollUntilCompleted(orderId: string, transactionId: string): void {
    this.vietQRPaymentService.pollByOrderId(orderId).subscribe({
      next: (status) => {
        this.completeAndNavigate(orderId, status.transactionId || transactionId);
      },
      error: (err) => {
        this.router.navigate(['/payment/failed'], {
          queryParams: {
            orderId
          },
          state: {
            orderId,
            reason: err?.message || 'Payment failed or timed out',
          },
        });
      }
    });
  }

  private completeAndNavigate(orderId: string, transactionId: string): void {
    this.step = 2;
    setTimeout(() => {
      this.orderService.confirmPaidOrder(orderId).subscribe({
        next: () => {
          this.cartService.clear();
          this.orderService.setCurrentOrderId(orderId);
          this.router.navigate(['/payment/success'], {
            queryParams: {
              orderId
            },
            state: {
              orderId,
              transactionId,
            },
          });
        },
        error: (err) => {
          this.router.navigate(['/payment/failed'], {
            queryParams: {
              orderId
            },
            state: {
              orderId,
              transactionId,
              reason: err?.error?.message || err?.message || 'Unable to confirm paid order',
            },
          });
        }
      });
    }, 1000);
  }
}
