import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { OrderStepperComponent } from '../../../order/components/order-stepper/order-stepper.component';

import { CartItemView } from '../../models/cart.model';

import { ChangeDetectorRef } from '@angular/core';
import { CartPageFacade } from '../../services/cart-page.facade';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, OrderStepperComponent],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent implements OnInit {

  private cartPageFacade = inject(CartPageFacade);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  cartItems: CartItemView[] = [];

  errorMsg = '';
  isLoading = false;

  ngOnInit(): void {
    this.loadCart();
  }

  private loadCart(): void {
    this.cartPageFacade.loadCartItems().subscribe(items => {
      this.cartItems = items;
      this.cdr.detectChanges(); 
    });
  }

  get subtotal(): number {
    return this.cartItems.reduce(
      (sum, item) => sum + item.unitPriceExVat * item.quantity,
      0
    );
  }

  get totalPayable(): number {
    return this.subtotal;
  }

  incrementQuantity(item: CartItemView): void {
    item.quantity++;
    item.stockError = undefined;

    this.cartPageFacade.updateQuantity(
      item.productId,
      item.quantity
    );
  }

  decrementQuantity(item: CartItemView): void {

    if (item.quantity <= 1) {
      return;
    }

    item.quantity--;
    item.stockError = undefined;

    this.cartPageFacade.updateQuantity(
      item.productId,
      item.quantity
    );
  }

  updateQuantityFromInput(item: CartItemView, rawValue: string): void {
    if (rawValue.trim() === '') {
      return;
    }

    const parsedQuantity = Number(rawValue);
    if (!Number.isFinite(parsedQuantity)) {
      return;
    }

    const nextQuantity = this.normalizeQuantity(item, parsedQuantity);
    this.setItemQuantity(item, nextQuantity);
  }

  normalizeQuantityInput(item: CartItemView, input: HTMLInputElement): void {
    const parsedQuantity = Number(input.value);
    const nextQuantity = this.normalizeQuantity(item, parsedQuantity);
    this.setItemQuantity(item, nextQuantity);
    input.value = String(nextQuantity);
  }

  removeItem(item: CartItemView): void {

    this.cartItems = this.cartItems.filter(
      i => i.productId !== item.productId
    );

    this.cartPageFacade.remove(item.productId);
  }

  proceedToCheckout(): void {

    if (!this.cartItems.length) {
      return;
    }

    this.isLoading = true;
    this.errorMsg = '';

    this.cartPageFacade.checkout().subscribe({

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

        this.applyStockIssues(err.error?.data);

        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  private applyStockIssues(issues: any): void {
    if (!Array.isArray(issues) || issues.length === 0) {
      return;
    }

    const issueByProductId = new Map<number, any>(
      issues.map(issue => [Number(issue.productId), issue])
    );

    const nextItems: CartItemView[] = [];

    for (const item of this.cartItems) {
      const issue = issueByProductId.get(item.productId);
      if (!issue) {
        nextItems.push({ ...item, stockError: undefined });
        continue;
      }

      const availableQuantity = Number(issue.availableQuantity ?? 0);
      const requestedQuantity = Number(issue.requestedQuantity ?? item.quantity);
      const lackingQuantity = Math.max(requestedQuantity - availableQuantity, 0);

      if (availableQuantity <= 0) {
        nextItems.push({
          ...item,
          availableQuantity,
          stockError: `Requested ${requestedQuantity}, available ${availableQuantity}, lacking ${lackingQuantity}. Please remove this item or update your cart.`
        });
        continue;
      }

      nextItems.push({
        ...item,
        availableQuantity,
        stockError: `Requested ${requestedQuantity}, available ${availableQuantity}, lacking ${lackingQuantity}. Please update your cart and checkout again.`
      });
    }

    this.cartItems = nextItems;
  }

  private normalizeQuantity(item: CartItemView, rawQuantity: number): number {
    let nextQuantity = Math.floor(rawQuantity);
    if (!Number.isFinite(nextQuantity) || nextQuantity < 1) {
      nextQuantity = 1;
    }

    item.stockError = undefined;

    return nextQuantity;
  }

  private setItemQuantity(item: CartItemView, quantity: number): void {
    item.quantity = quantity;
    this.cartPageFacade.updateQuantity(
      item.productId,
      quantity
    );
  }
}
