import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { OrderStepperComponent } from '../../../order/components/order-stepper/order-stepper.component';

import { CartItemRequest, CartItemView } from '../../models/cart.model';

import { CartService } from '../../services/cart.service';
import { ProductService } from '../../../product/services/product.service';
import { OrderService } from '../../../order/services/order.service';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, OrderStepperComponent],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent implements OnInit {

  private cartService = inject(CartService);
  private productService = inject(ProductService);
  private orderService = inject(OrderService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  cartItems: CartItemView[] = [];

  vatRate = 0.1;
  errorMsg = '';
  isLoading = false;

  ngOnInit(): void {
    this.loadCart();
  }

  private loadCart(): void {
    const cart = this.cartService.getCart();

    if (!cart.length) {
      this.cartItems = [];
      return;
    }

    const ids = cart.map(i => i.productId);

    this.productService.getByIds(ids).subscribe(products => {
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
      console.log('cartItems', this.cartItems);
      this.cdr.detectChanges(); 
    });
  }

  get subtotal(): number {
    return this.cartItems.reduce(
      (sum, item) => sum + item.unitPriceExVat * item.quantity,
      0
    );
  }

  get vat(): number {
    return Math.round(this.subtotal * this.vatRate);
  }

  get totalPayable(): number {
    return this.subtotal + this.vat;
  }

  incrementQuantity(item: CartItemView): void {
    if(item.availableQuantity === undefined) {
      return;
    }

    if (item.quantity >= item.availableQuantity) {
      return;
    }

    item.quantity++;

    this.cartService.updateQuantity(
      item.productId,
      item.quantity
    );
  }

  decrementQuantity(item: CartItemView): void {

    if (item.quantity <= 1) {
      return;
    }

    item.quantity--;

    this.cartService.updateQuantity(
      item.productId,
      item.quantity
    );
  }

  removeItem(item: CartItemView): void {

    this.cartItems = this.cartItems.filter(
      i => i.productId !== item.productId
    );

    this.cartService.remove(item.productId);
  }

  proceedToCheckout(): void {

    if (!this.cartItems.length) {
      return;
    }

    this.isLoading = true;
    this.errorMsg = '';

    const requestItems: CartItemRequest[] =
      this.cartService.toRequestItems();

    this.orderService.placeOrder(requestItems).subscribe({

      next: (res) => {

        this.isLoading = false;

        if (res.success) {
          this.router.navigate(['/delivery']);
        }
        else {
          this.errorMsg = res.message;
          this.cdr.detectChanges();
        }
      },

      error: (err) => {

        this.isLoading = false;

        this.errorMsg =
          err.error?.message ??
          'An error occurred while validating the order.';

        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }
}