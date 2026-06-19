import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { finalize } from 'rxjs';

import { OrderStepperComponent } from '../../components/order-stepper/order-stepper.component';

import { CartService } from '../../../cart/services/cart.service';
import { CartItemView } from '../../../cart/models/cart.model';

import { DeliveryInfoRequest } from '../../models/order.model';
import { CheckoutFlowFacade } from '../../services/checkout-flow.facade';

import { ProductService } from '../../../product/services/product.service';

@Component({
  selector: 'app-delivery',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    OrderStepperComponent
  ],
  templateUrl: './delivery.component.html',
  styleUrl: './delivery.component.scss'
})
export class DeliveryComponent implements OnInit {

  private cartService = inject(CartService);
  private checkoutFlowFacade = inject(CheckoutFlowFacade);
  private productService = inject(ProductService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  deliveryInfo: DeliveryInfoRequest = {
    recipientName: '',
    phoneNumber: '',
    email: '',
    deliveryProvince: '',
    detailAddress: '',
    note: ''
  };

  subtotal = 0;
  cartItems: CartItemView[] = [];

  errorMsg = '';
  isLoading = false;

  readonly provinces = [
    'An Giang',
    'Bac Ninh',
    'Ca Mau',
    'Cao Bang',
    'Can Tho City',
    'Da Nang City',
    'Dak Lak',
    'Dien Bien',
    'Dong Nai',
    'Dong Thap',
    'Gia Lai',
    'Ha Noi',
    'Ha Tinh',
    'Hai Phong City',
    'Ho Chi Minh City',
    'Hue City',
    'Hung Yen',
    'Khanh Hoa',
    'Lai Chau',
    'Lam Dong',
    'Lang Son',
    'Lao Cai',
    'Nghe An',
    'Ninh Binh',
    'Phu Tho',
    'Quang Ngai',
    'Quang Ninh',
    'Quang Tri',
    'Son La',
    'Tay Ninh',
    'Thai Nguyen',
    'Thanh Hoa',
    'Tuyen Quang',
    'Vinh Long'
  ];

  ngOnInit(): void {

    const cart = this.cartService.getCart();

    if (cart.length === 0) {
      this.router.navigate(['/cart']);
      return;
    }

    const ids = cart.map(i => i.productId);

    this.productService.getByIds(ids)
      .subscribe({
        next: products => {

          this.cartItems = products.map(product => {
            const cartItem =
              cart.find(c => c.productId === product.productId)!;

            return {
              productId: product.productId,
              title: product.title,
              category: product.productType,
              image: product.image,
              unitPriceExVat: product.sellingPrice,
              availableQuantity: product.quantityInStock,
              quantity: cartItem.quantity
            };
          });

          this.subtotal = this.cartItems.reduce((sum, item) => {

            return sum + item.unitPriceExVat * item.quantity;

          }, 0);

          this.cdr.detectChanges();
        },
        error: err => {
          console.error(err);
          this.errorMsg =
            'Unable to load product information.';
        }
      });
  }

  get totalPayable(): number {
    return this.subtotal;
  }

  reviewInvoice(): void {

    if (
      !this.deliveryInfo.recipientName ||
      !this.deliveryInfo.phoneNumber ||
      !this.deliveryInfo.deliveryProvince ||
      !this.deliveryInfo.detailAddress
    ) {

      this.errorMsg =
        'Please fill all required fields.';

      return;
    }

    if (!this.deliveryInfo.phoneNumber.trim().match(/^\d{10}$/)) {
      this.errorMsg = 'Phone number must contain exactly 10 digits.';
      return;
    }

    const email = this.deliveryInfo.email?.trim();
    if (email && !email.match(/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/)) {
      this.errorMsg = 'Email address is invalid.';
      return;
    }

    const requestItems =
      this.cartService.toRequestItems();

    if (requestItems.length === 0) {
      this.router.navigate(['/cart']);
      return;
    }

    this.isLoading = true;
    this.errorMsg = '';

    this.checkoutFlowFacade
      .createInvoiceFromCurrentCart(this.deliveryInfo)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: res => {

          if (!res.success) {
            this.errorMsg = res.message;
            return;
          }

          const orderId = res.data?.orderId;
          if (!orderId) {
            this.errorMsg = 'Invoice was created but the order ID was not returned.';
            return;
          }

          this.checkoutFlowFacade.setCurrentOrderId(orderId);

          this.router.navigate(['/invoice'], {
            queryParams: {
              orderId
            }
          });
        },
        error: err => {

          this.errorMsg =
            err.error?.message ||
            err.message ||
            'Unable to create invoice.';

          console.error(err);
        }
      });
  }
}
