import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderStepperComponent } from '../../components/order-stepper/order-stepper.component';
import { InvoiceScreenResponse } from '../../models/order.model';
import { OrderService } from '../../services/order.service';
import { CartItem } from '../../../cart/models/cart.model';
import { finalize, retry, Subject, takeUntil, timeout, timer } from 'rxjs';

@Component({
  selector: 'app-invoice',
  standalone: true,
  imports: [CommonModule, OrderStepperComponent],
  templateUrl: './invoice.component.html',
  styleUrl: './invoice.component.scss'
})
/**
 * Coupling: Data coupling through InvoiceResponse and DeliveryInfoRequest view state.
 * Cohesion: Functional cohesion because it renders an invoice and starts payment navigation.
 */
export class InvoiceComponent implements OnInit, OnDestroy {
  private orderService = inject(OrderService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();

  invoice: InvoiceScreenResponse | null = null;

  orderId = '';
  issueDate = '';

  recipient = {
    name: '',
    phone: '',
    email: ''
  };

  shipping = {
    address: '',
    province: ''
  };

  cartItems: CartItem[] = [];

  subtotal = 0;
  vat = 0;
  shippingFee = 0;
  totalPayable = 0;
  isConfirmingPaid = false;
  isLoading = false;
  errorMsg = '';

  ngOnInit() {
    this.route.queryParamMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const orderId = params.get('orderId') || this.orderService.getCurrentOrderId();
        if (!orderId) {
          this.router.navigate(['/cart']);
          return;
        }
        this.orderService.setCurrentOrderId(orderId);
        this.loadInvoice(orderId);
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  proceedToPayment() {
    if (!this.orderId) return;

    this.router.navigate(
      ['/payment/vietqr'],
      {
        queryParams: {
          orderId: this.orderId
        }
      }
    );
  }

  private loadInvoice(orderId: string): void {
    this.isLoading = true;
    this.errorMsg = '';

    this.orderService.getInvoiceScreen(orderId)
      .pipe(
        timeout(10000),
        retry({
          count: 1,
          delay: () => timer(500)
        }),
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: inv => {
          this.invoice = inv;
          this.orderId = inv.orderId;
          this.issueDate = inv.issueDate
            ? new Date(inv.issueDate).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
            : new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });

          this.cartItems = (inv.lineItems || []).map(item => ({
            productId: item.productId ?? 0,
            title: item.productTitle,
            category: item.category || '',
            unitPriceExVat: item.unitSellingPrice,
            quantity: item.quantity,
            image: item.image || '/assets/book-cover.png'
          }));

          this.subtotal = inv.totalProductPriceExclVat;
          this.vat = inv.totalProductPriceInclVat - inv.totalProductPriceExclVat;
          this.shippingFee = inv.deliveryFee;
          this.totalPayable = inv.totalAmountToBePaid;
          this.recipient = {
            name: inv.recipientName || '',
            phone: inv.phoneNumber || '',
            email: inv.email || ''
          };
          this.shipping = {
            address: inv.detailAddress || '',
            province: inv.province || ''
          };
        },
        error: err => {
          this.invoice = null;
          this.errorMsg =
            err?.error?.message ||
            err?.error?.error ||
            `Unable to load invoice for order ${orderId}.`;
        }
      });
  }
}
