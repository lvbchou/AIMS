import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { VietQRPaymentService } from '../../services/vietqr-payment.service';
import { OrderConfirmationData } from '../../models/payment.model';
import { CartService } from '../../../cart/services/cart.service';
import { OrderService } from '../../../order/services/order.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { finalize, retry, timeout, timer } from 'rxjs';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-success.component.html',
  styleUrls: ['./payment-success.component.scss'],
})
export class PaymentSuccessComponent implements OnInit {

  orderReference = '';
  isLoading = false;
  errorMsg = '';
  confirmation: OrderConfirmationData | null = null;

  private orderId = '';
  private transactionId = '';

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly vietQRPaymentService: VietQRPaymentService,
    private readonly cartService: CartService,
    private readonly orderService: OrderService,
    private readonly toastService: ToastService,
    private readonly cdr: ChangeDetectorRef,
  ) { }

  ngOnInit(): void {
    const state = history.state;
    this.orderId =
      state?.orderId ||
      this.route.snapshot.queryParamMap.get('orderId') ||
      this.orderService.getCurrentOrderId() ||
      '';
    this.transactionId = state?.transactionId || '';

    this.cartService.clear();
    this.showSuccessToastOnce();

    if (this.orderId) {
      const shortId = this.orderId.replace(/-/g, '').slice(-8).toUpperCase();
      this.orderReference = `Order #${shortId}`;
      this.loadConfirmation();
    } else {
      this.orderReference = this.transactionId || 'N/A';
    }
  }

  backToShop(): void {
    this.router.navigate(['/']);
  }

  formatCurrency(amount: number | undefined): string {
    return `${(amount ?? 0).toLocaleString('vi-VN')} VND`;
  }

  private loadConfirmation(): void {
    this.isLoading = true;
    this.errorMsg = '';
    this.vietQRPaymentService.getOrderConfirmation(this.orderId)
      .pipe(
        timeout(10000),
        retry({
          count: 3,
          delay: () => timer(1000)
        }),
        finalize(() => {
          this.isLoading = false;
          this.orderService.clearCheckoutState();
          this.cdr.detectChanges();
        })
      )
      .subscribe({
      next: (confirmation: OrderConfirmationData) => {
        this.confirmation = confirmation;
        if (confirmation?.orderName) {
          this.orderReference = confirmation.orderName;
        }
      },
      error: (err) => {
        this.errorMsg = err?.message || 'Unable to load order details.';
      }
    });
  }

  private showSuccessToastOnce(): void {
    const key = `aims_order_success_toast_${this.orderId || this.transactionId || 'unknown'}`;
    if (sessionStorage.getItem(key)) {
      return;
    }
    sessionStorage.setItem(key, 'shown');
    this.toastService.show('Order placed successfully');
  }
}
