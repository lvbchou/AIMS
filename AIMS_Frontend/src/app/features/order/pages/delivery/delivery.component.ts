import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { finalize, switchMap } from 'rxjs';

import { OrderStepperComponent } from '../../components/order-stepper/order-stepper.component';

import { CartService } from '../../../cart/services/cart.service';
import { CartItemRequest } from '../../../cart/models/cart.model';

import { OrderService } from '../../services/order.service';
import { DeliveryInfoRequest } from '../../models/order.model';

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
  private orderService = inject(OrderService);
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
  vat = 0;
  shippingFee: number | null = null;

  errorMsg = '';
  isLoading = false;

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

          this.subtotal = products.reduce((sum, product) => {

            const cartItem =
              cart.find(c => c.productId === product.productId)!;

            return (
              sum +
              product.sellingPrice * cartItem.quantity
            );

          }, 0);

          this.vat = Math.round(this.subtotal * 0.1);

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
    return (
      this.subtotal +
      this.vat +
      (this.shippingFee ?? 0)
    );
  }

  onProvinceChange(): void {

    if (!this.deliveryInfo.deliveryProvince) {
      return;
    }

    this.orderService
      .calculateShipping(this.deliveryInfo)
      .subscribe({
        next: res => {

          if (res.success) {
            this.shippingFee = res.data;
          } else {
            this.shippingFee = null;
            this.errorMsg = res.message;
          }

        },
        error: err => {

          this.shippingFee = null;

          this.errorMsg =
            err.error?.message ??
            'Unable to calculate shipping fee.';

          console.error(err);
        }
      });
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

    const requestItems =
      this.cartService.toRequestItems();

    if (requestItems.length === 0) {
      this.router.navigate(['/cart']);
      return;
    }

    this.isLoading = true;
    this.errorMsg = '';

    this.orderService
      .placeOrder(requestItems)
      .pipe(
        switchMap(() =>
          this.orderService.createInvoice(
            this.deliveryInfo
          )
        ),
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

          this.orderService
            .setCurrentDeliveryInfo(
              this.deliveryInfo
            );

          this.orderService
            .setCurrentInvoice(
              res.data
            );

          this.router.navigate(['/invoice']);
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