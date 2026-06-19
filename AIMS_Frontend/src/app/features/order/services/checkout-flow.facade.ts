import { Injectable } from '@angular/core';
import { Observable, switchMap } from 'rxjs';
import { CartService } from '../../cart/services/cart.service';
import { ApiResponse, DeliveryInfoRequest, InvoiceResponse } from '../models/order.model';
import { OrderService } from './order.service';

@Injectable({ providedIn: 'root' })
export class CheckoutFlowFacade {
  constructor(
    private cartService: CartService,
    private orderService: OrderService
  ) {}

  createInvoiceFromCurrentCart(deliveryInfo: DeliveryInfoRequest): Observable<ApiResponse<InvoiceResponse>> {
    const requestItems = this.cartService.toRequestItems();
    return this.orderService.placeOrder(requestItems).pipe(
      switchMap(() => this.orderService.createInvoice(deliveryInfo, requestItems))
    );
  }

  setCurrentOrderId(orderId: string): void {
    this.orderService.setCurrentOrderId(orderId);
  }
}
