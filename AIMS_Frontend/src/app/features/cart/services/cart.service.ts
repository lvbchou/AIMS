// ==========================================================
// FILE: src/app/core/services/cart.service.ts
// Cart state service — real-time item count từ backend
// ==========================================================

import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { CartItemRequest } from '../models/cart.model';
import { CartMutationPolicy } from './cart-mutation.policy';
import { CartStorageGateway } from './cart-storage.gateway';

@Injectable({ providedIn: 'root' })
export class CartService {
  private cartSubject: BehaviorSubject<CartItemRequest[]>;

  cart$: Observable<CartItemRequest[]>;

  constructor(
    private storage: CartStorageGateway,
    private mutationPolicy: CartMutationPolicy
  ) {
    this.cartSubject = new BehaviorSubject<CartItemRequest[]>(this.storage.load());
    this.cart$ = this.cartSubject.asObservable();
  }

  getCart(): CartItemRequest[] {
    return this.cartSubject.value;
  }

  setCart(items: CartItemRequest[]): void {
    this.storage.save(items);
    this.cartSubject.next(items);
  }

  addToCart(productId: number, quantity: number = 1): void {

    this.setCart(this.mutationPolicy.add(this.getCart(), productId, quantity));
  }

  updateQuantity(
    productId: number,
    quantity: number
  ): void {

    this.setCart(this.mutationPolicy.updateQuantity(this.getCart(), productId, quantity));
  }

  remove(productId: number): void {

    this.setCart(this.mutationPolicy.remove(this.getCart(), productId));
  }

  clear(): void {
    this.storage.clear();
    this.cartSubject.next([]);
  }

  getItemCount(): number {
    return this.getCart()
      .reduce((sum, i) => sum + i.quantity, 0);
  }

  getDifferentItemCount(): number {
    return this.getCart().length;
  }

  toRequestItems(): CartItemRequest[] {
    return this.getCart();
  }
}
