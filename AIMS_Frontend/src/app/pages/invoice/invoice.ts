import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Stepper } from '../../components/stepper/stepper';
import { DeliveryInfoRequest, InvoiceResponse, OrderService } from '../../services/order.service';

interface CartItem {
  productId: number;
  title: string;
  category: string;
  unitPriceExVat: number;
  quantity: number;
  imageUrl: string;
}

@Component({
  selector: 'app-invoice',
  standalone: true,
  imports: [CommonModule, Stepper],
  templateUrl: './invoice.html',
  styleUrl: './invoice.css'
})
/**
 * Coupling: Data coupling through InvoiceResponse and DeliveryInfoRequest view state.
 * Cohesion: Functional cohesion because it renders an invoice and starts payment navigation.
 */
export class Invoice implements OnInit {
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
    this.router.navigate(['/payment']);
  }
}
