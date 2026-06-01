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
    // 1. Retrieve the payment token from URL query params
    this.route.queryParams.subscribe(params => {
      const token = params['token'] || params['paymentId'];
      const isSuccessParam = params['success'];

      // If redirected from an immediate frontend error or cancel
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

      // 2. Complete/Capture the payment session on the backend
      this.capturePayment(token);
    });
  }

  private capturePayment(token: string): void {
    console.log('[PaymentResult] Initiating capture for token:', token);
    this.paymentService.completePayment(token).subscribe({
      next: (res) => {
        console.log('[PaymentResult] Capture completed successfully:', res);
        this.loading = false;
        this.success = true;
        // Format a high-fidelity reference code exactly as in the success screenshot
        this.orderReference = '#AIMS-' + this.generateRandomRef() + '-SUC';
        this.orderService.clearCheckoutState();
  
        // Wrap trong setTimeout để tránh ExpressionChangedAfterItHasBeenChecked
        setTimeout(() => {
          this.cartService.clear();
        }, 0);
        this.cdr.detectChanges(); // Force Angular to update view
      },
      error: (err) => {
        console.error('[PaymentResult] Capture failed:', err);
        this.loading = false;
        this.success = false;
        this.orderReference = '#AIMS-' + this.generateRandomRef() + '-ERR';
        this.errorMessage = err?.error?.error || err?.message || 'Transaction capture execution failed.';
        this.cdr.detectChanges(); // Force Angular to update view
      }
    });
  }

  private generateRandomRef(): string {
    // Generates a mock number like '82910' for high-fidelity matching
    return Math.floor(10000 + Math.random() * 90000).toString();
  }

  onRetry(): void {
    this.router.navigate(['/payment']);
  }
}
