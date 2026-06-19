import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { ApiResponse } from '../../order/models/order.model';
import { CartItemRequest, CartItemView } from '../models/cart.model';
import { CartService } from './cart.service';
import { OrderService } from '../../order/services/order.service';
import { ProductService } from '../../product/services/product.service';

@Injectable({ providedIn: 'root' })
export class CartPageFacade {
  constructor(
    private cartService: CartService,
    private productService: ProductService,
    private orderService: OrderService
  ) {}

  loadCartItems(): Observable<CartItemView[]> {
    const cart = this.cartService.getCart();
    if (!cart.length) {
      return of([]);
    }

    const ids = cart.map(item => item.productId);
    return this.productService.getByIds(ids).pipe(
      map(products => products.map(product => {
        const cartItem = cart.find(item => item.productId === product.productId)!;
        return {
          productId: product.productId,
          title: product.title,
          category: product.productType,
          image: product.image,
          unitPriceExVat: product.sellingPrice,
          availableQuantity: product.quantityInStock,
          quantity: cartItem.quantity
        };
      }))
    );
  }

  checkout(): Observable<ApiResponse<string>> {
    return this.orderService.placeOrder(this.getRequestItems());
  }

  getRequestItems(): CartItemRequest[] {
    return this.cartService.toRequestItems();
  }

  updateQuantity(productId: number, quantity: number): void {
    this.cartService.updateQuantity(productId, quantity);
  }

  remove(productId: number): void {
    this.cartService.remove(productId);
  }
}
