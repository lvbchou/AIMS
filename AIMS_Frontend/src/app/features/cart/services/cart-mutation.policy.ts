import { Injectable } from '@angular/core';
import { CartItemRequest } from '../models/cart.model';

@Injectable({ providedIn: 'root' })
export class CartMutationPolicy {
  add(items: CartItemRequest[], productId: number, quantity: number): CartItemRequest[] {
    const existing = items.find(item => item.productId === productId);
    if (existing) {
      return items.map(item =>
        item.productId === productId
          ? { ...item, quantity: item.quantity + quantity }
          : item
      );
    }

    return [
      ...items,
      { productId, quantity }
    ];
  }

  updateQuantity(items: CartItemRequest[], productId: number, quantity: number): CartItemRequest[] {
    return items.map(item =>
      item.productId === productId
        ? { ...item, quantity }
        : item
    );
  }

  remove(items: CartItemRequest[], productId: number): CartItemRequest[] {
    return items.filter(item => item.productId !== productId);
  }
}
