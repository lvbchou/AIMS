// ==========================================================
// FILE: src/app/core/services/cart.service.ts
// Cart state service — real-time item count từ backend
// ==========================================================

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, timer } from 'rxjs';
import { switchMap, catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CartItemRequest, CartItemView } from '../models/cart.model';

@Injectable({ providedIn: 'root' })
export class CartService {

  private readonly STORAGE_KEY = 'aims_cart';

  private cartSubject = new BehaviorSubject<CartItemRequest[]>(this.load());

  cart$ = this.cartSubject.asObservable();

  constructor() {}

  getCart(): CartItemRequest[] {
    return this.cartSubject.value;
  }

  setCart(items: CartItemRequest[]): void {
    localStorage.setItem(
      this.STORAGE_KEY,
      JSON.stringify(items)
    );

    this.cartSubject.next(items);
  }

  addToCart(productId: number, quantity: number = 1): void {

    const current = this.getCart();

    const existing = current.find(
      i => i.productId === productId
    );

    if (existing) {

      const next = current.map(i =>
        i.productId === productId
          ? {
              ...i,
              quantity: i.quantity + quantity
            }
          : i
      );

      this.setCart(next);

      return;
    }

    this.setCart([
      ...current,
      {
        productId,
        quantity
      }
    ]);
  }

  updateQuantity(
    productId: number,
    quantity: number
  ): void {

    const next = this.getCart().map(i =>
      i.productId === productId
        ? { ...i, quantity }
        : i
    );

    this.setCart(next);
  }

  remove(productId: number): void {

    const next = this.getCart().filter(
      i => i.productId !== productId
    );

    this.setCart(next);
  }

  clear(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.cartSubject.next([]);
  }

  getItemCount(): number {
    return this.getCart()
      .reduce((sum, i) => sum + i.quantity, 0);
  }

  getDifferentItemCount(): number {
    return this.getCart().length;
  }

  private load(): CartItemRequest[] {

    try {

      const raw = localStorage.getItem(
        this.STORAGE_KEY
      );

      if (!raw) {
        return [];
      }

      return JSON.parse(raw);

    } catch {

      localStorage.removeItem(
        this.STORAGE_KEY
      );

      return [];
    }
  }

  toRequestItems(): CartItemRequest[] {
    return this.getCart();
  }
}