import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { OrderStepperComponent } from '../../components/order-stepper/order-stepper.component';
import { DeliveryInfoRequest, InvoiceResponse } from '../../models/order.model';
import { OrderService } from '../../services/order.service';
import { CartItem } from '../../../cart/models/cart.model';

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
export class InvoiceComponent implements OnInit {
  private orderService = inject(OrderService);
  private router = inject(Router);

  invoice: InvoiceResponse | null = null;
  deliveryInfo: DeliveryInfoRequest | null = null;

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
  errorMsg = '';

  ngOnInit() {
    this.orderService.currentInvoice$.subscribe(inv => {
      if (!inv) {
        this.router.navigate(['/cart']);
        return;
      }
      this.invoice = inv;
      this.orderId = inv.orderId;
      this.issueDate = inv.issueDate
        ? new Date(inv.issueDate).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
        : new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });

      this.cartItems = inv.items.map(item => ({
        productId: item.productId,
        title: item.title,
        category: item.category,
        unitPriceExVat: item.unitPriceExVat,
        quantity: item.quantity,
        imageUrl: item.image || '/assets/book-cover.png'
      }));
      this.subtotal = inv.subtotalExVat;
      this.vat = inv.vat;
      this.shippingFee = inv.shippingFee;
      this.totalPayable = inv.total;
    });

    this.orderService.currentDeliveryInfo$.subscribe(info => {
      if (info) {
        this.deliveryInfo = info;
        this.recipient = {
          name: info.recipientName,
          phone: info.phoneNumber,
          email: info.email
        };
        this.shipping = {
          address: info.detailAddress,
          province: info.deliveryProvince
        };
      }
    });
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
}
