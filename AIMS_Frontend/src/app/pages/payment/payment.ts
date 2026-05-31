import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Stepper } from '../../components/stepper/stepper';
import { InvoiceResponse, OrderService } from '../../services/order.service';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, Stepper],
  templateUrl: './payment.html',
  styleUrl: './payment.css'
})
/**
 * Coupling: Data coupling through orderId when invoking post-payment confirmation.
 * Cohesion: Sequential cohesion for mock confirmation followed by checkout cleanup.
 */
export class Payment implements OnInit {
  private orderService = inject(OrderService);
  private router = inject(Router);

  invoice: InvoiceResponse | null = null;
  isConfirming = false;
  errorMsg = '';
  paymentTerms = '10:00';
  qrCells = Array.from({ length: 144 }, (_, index) => index);

  ngOnInit() {
    this.invoice = this.orderService.getCurrentInvoice();
    if (!this.invoice) {
      this.router.navigate(['/cart']);
    }
  }

  confirmPayment() {
    if (!this.invoice?.orderId) return;
    this.isConfirming = true;
    this.errorMsg = '';

    this.orderService.confirmPaidOrder(this.invoice.orderId).subscribe({
      next: (res) => {
        this.isConfirming = false;
        if (res.success) {
          this.orderService.setCurrentInvoice(res.data);
          this.orderService.clearCart();
          this.orderService.clearCheckoutState();
          this.router.navigate(['/success']);
        } else {
          this.errorMsg = res.message;
        }
      },
      error: (err) => {
        this.isConfirming = false;
        this.errorMsg = err.error?.message || err.message || 'Unable to confirm payment.';
        console.error(err);
      }
    });
  }
}
