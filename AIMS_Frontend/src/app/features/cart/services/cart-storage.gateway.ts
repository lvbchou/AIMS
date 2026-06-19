import { Injectable } from '@angular/core';
import { CartItemRequest } from '../models/cart.model';

@Injectable({ providedIn: 'root' })
export class CartStorageGateway {
  private readonly storageKey = 'aims_cart';

  load(): CartItemRequest[] {
    try {
      const raw = localStorage.getItem(this.storageKey);
      if (!raw) {
        return [];
      }
      return JSON.parse(raw);
    } catch {
      localStorage.removeItem(this.storageKey);
      return [];
    }
  }

  save(items: CartItemRequest[]): void {
    localStorage.setItem(this.storageKey, JSON.stringify(items));
  }

  clear(): void {
    localStorage.removeItem(this.storageKey);
  }
}
