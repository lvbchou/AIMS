import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../services/paypal-payment.service';

@Component({
  selector: 'app-payment-redirect',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-redirect.component.html',
  styleUrl: './payment-redirect.component.scss'
})
export class PaymentRedirectComponent implements OnInit {
  amount = 126500;
  orderId = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    const state = history.state;
    this.orderId = state?.orderId || '';
    if (state?.amount && Number(state.amount) > 0) {
      this.amount = Number(state.amount);
    }

    // 1. Get amount from query parameters
    this.route.queryParams.subscribe(params => {
      const amt = Number(params['amount']);
      if (!isNaN(amt) && amt > 0) {
        this.amount = amt;
      }
      this.orderId = params['orderId'] || this.orderId;
      
      // 2. Initiate payment session
      this.initiatePayPalPayment();
    });
  }

  private initiatePayPalPayment(): void {
    if (!this.orderId) {
      this.handleError('Missing order information for PayPal payment.');
      return;
    }

    this.paymentService.initiatePayment(this.amount, this.orderId).subscribe({
      next: (response) => {
        if (response?.approvalUrl) {
          // Redirect the browser window to PayPal Sandbox
          window.location.href = response.approvalUrl;
        } else {
          this.handleError('No redirect URL returned from gateway.');
        }
      },
      error: (err) => {
        const errorMsg = err?.error?.error || err?.message || 'Gateway communication failed';
        this.handleError(errorMsg);
      }
    });
  }

  private handleError(message: string): void {
    // Navigate to payment result screen with failed status
    this.router.navigate(['/payment/result'], {
      queryParams: {
        token: 'ERROR',
        success: 'false',
        error: message
      }
    });
  }
}
