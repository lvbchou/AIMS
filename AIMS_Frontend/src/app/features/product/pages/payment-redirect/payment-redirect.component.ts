import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../../../core/services/payment.service';

@Component({
  selector: 'app-payment-redirect',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-redirect.component.html',
  styleUrl: './payment-redirect.component.scss'
})
export class PaymentRedirectComponent implements OnInit {
  amount = 126500;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    // 1. Get amount from query parameters
    this.route.queryParams.subscribe(params => {
      const amt = Number(params['amount']);
      if (!isNaN(amt) && amt > 0) {
        this.amount = amt;
      }
      
      // 2. Initiate payment session
      this.initiatePayPalPayment();
    });
  }

  private initiatePayPalPayment(): void {
    this.paymentService.initiatePayment(this.amount).subscribe({
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
