import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderCancellationDetails } from '../../models/order.model';
import { OrderService } from '../../services/order.service';
import { finalize, Subject, takeUntil, timeout } from 'rxjs';

@Component({
  selector: 'app-order-cancel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cancel.component.html',
  styleUrl: './cancel.component.scss'
})
export class OrderCancelComponent implements OnInit, OnDestroy {
  private orderService = inject(OrderService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();

  details: OrderCancellationDetails | null = null;
  orderId = '';
  isLoading = false;
  isCanceling = false;
  errorMsg = '';
  successMsg = '';
  vietQrManualMsg = '';
  showConfirmPopup = false;

  ngOnInit() {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const id = params.get('orderId');
        if (!id) {
          this.router.navigate(['/']);
          return;
        }
        this.orderId = id;
        this.loadCancellationDetails(id);
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadCancellationDetails(orderId: string): void {
    this.isLoading = true;
    this.errorMsg = '';

    this.orderService.getCancellationDetails(orderId)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: data => {
          this.details = data;
        },
        error: err => {
          this.details = null;
          this.errorMsg = err?.error?.message || err?.error?.error || `Unable to load cancellation details for order ${orderId}.`;
        }
      });
  }

  openConfirmDialog() {
    if (!this.details || !this.details.eligibleForCancellation) return;
    this.showConfirmPopup = true;
  }

  closeConfirmDialog() {
    this.showConfirmPopup = false;
  }

  confirmCancelOrder() {
    this.showConfirmPopup = false;
    this.isCanceling = true;
    this.errorMsg = '';
    this.successMsg = '';
    this.vietQrManualMsg = '';

    this.orderService.cancelOrder(this.orderId)
      .pipe(
        timeout(15000),
        finalize(() => {
          this.isCanceling = false;
          this.cdr.detectChanges();
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: response => {
          if (response.success) {
            this.successMsg = response.message || 'Your order has been successfully cancelled and fully refunded.';
            this.loadCancellationDetails(this.orderId);
          } else {
            if (response.data === 'VIETQR_MANUAL_REFUND') {
              this.vietQrManualMsg = response.message;
            } else {
              this.errorMsg = response.message || 'Failed to cancel the order.';
            }
          }
        },
        error: err => {
          this.errorMsg = err?.error?.message || err?.error?.error || 'A network error occurred while canceling the order.';
        }
      });
  }

  goHome() {
    this.router.navigate(['/']);
  }
}
