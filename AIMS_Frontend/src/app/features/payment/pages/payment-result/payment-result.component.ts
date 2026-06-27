import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PaymentService } from '../../services/payment.service';
import { CartService } from '../../../cart/services/cart.service';
import { OrderService } from '../../../order/services/order.service';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './payment-result.component.html',
  styleUrl: './payment-result.component.scss'
})
export class PaymentResultComponent implements OnInit {
  loading = true;
  success = false;
  orderReference = '';
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private cdr: ChangeDetectorRef,
    private cartService: CartService,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'] || params['paymentId'];
      const isSuccessParam = params['success'];

      if (token === 'ERROR' || isSuccessParam === 'false') {
        this.loading = false;
        this.success = false;
        this.orderReference = '#AIMS-' + this.generateRandomRef() + '-ERR';
        this.errorMessage = params['error'] || 'Unfortunately, we are unable to process your payment.';
        return;
      }

      if (!token) {
        this.loading = false;
        this.success = false;
        this.orderReference = '#AIMS-' + this.generateRandomRef() + '-ERR';
        this.errorMessage = 'No active payment transaction session was detected.';
        return;
      }

      this.capturePayment(token);
    });
  }

  private capturePayment(token: string): void {
    this.paymentService.completePayment(token).subscribe({
      next: (res) => {
        if (!res.orderId) {
          this.showFailure('Payment was captured, but the order information was missing.');
          return;
        }

        this.orderService.confirmPaidOrder(res.orderId).subscribe({
          next: () => {
            this.loading = false;
            this.success = true;
            this.orderService.setCurrentOrderId(res.orderId!);
            this.cartService.clear();
            this.router.navigate(['/payment/success'], {
              queryParams: {
                orderId: res.orderId,
              },
              state: {
                orderId: res.orderId,
                transactionId: res.transactionId,
              },
            });
            this.cdr.detectChanges();
          },
          error: (err) => {
            this.showFailure(
              err?.error?.message ||
              err?.error?.error ||
              err?.message ||
              'Unable to confirm paid order.'
            );
          }
        });
      },
      error: (err) => {
        this.showFailure(
          err?.error?.message ||
          err?.error?.error ||
          err?.message ||
          'Transaction capture execution failed.'
        );
      }
    });
  }

  private showFailure(message: string): void {
    this.loading = false;
    this.success = false;
    this.orderReference = '#AIMS-' + this.generateRandomRef() + '-ERR';
    this.errorMessage = message;
    this.cdr.detectChanges();
  }

  private generateRandomRef(): string {
    return Math.floor(10000 + Math.random() * 90000).toString();
  }

  onRetry(): void {
    this.router.navigate(['/payment']);
  }
}
