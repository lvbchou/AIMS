import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Stepper } from '../../components/stepper/stepper';
import { CartItemRequest, CartItemView, OrderService } from '../../services/order.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, Stepper],
  templateUrl: './cart.html',
  styleUrl: './cart.css'
})
/**
 * Coupling: Data coupling with OrderService through cart-line values.
 * Cohesion: Functional cohesion because this page presents and submits the cart.
 */
export class Cart implements OnInit {
  private orderService = inject(OrderService);
  private router = inject(Router);

  cartItems: CartItemView[] = [];
  vatRate = 0.1;
  errorMsg = '';
  isLoading = false;

  ngOnInit() {
    this.cartItems = this.orderService.getCurrentCart();
  }

  get subtotal(): number {
    return this.cartItems.reduce((sum, item) => sum + item.unitPriceExVat * item.quantity, 0);
  }

  get vat(): number {
    return Math.round(this.subtotal * this.vatRate);
  }

  get totalPayable(): number {
    return this.subtotal + this.vat;
  }

  incrementQuantity(item: CartItemView): void {
    item.quantity++;
    this.persistCart();
  }

  decrementQuantity(item: CartItemView): void {
    if (item.quantity > 1) {
      item.quantity--;
      this.persistCart();
    }
  }

  removeItem(item: CartItemView): void {
    this.cartItems = this.cartItems.filter(i => i.productId !== item.productId);
    this.persistCart();
  }

  proceedToCheckout(): void {
    if (this.cartItems.length === 0) return;
    this.isLoading = true;
    this.errorMsg = '';

    const requestItems: CartItemRequest[] = this.cartItems.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }));

    this.orderService.placeOrder(requestItems).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success) {
          this.persistCart();
          this.router.navigate(['/delivery']);
        } else {
          this.errorMsg = res.message;
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg = err.error?.message || 'An error occurred while validating the order.';
        console.error(err);
      }
    });
  }

  private persistCart() {
    this.orderService.setCurrentCart(this.cartItems);
  }
}
