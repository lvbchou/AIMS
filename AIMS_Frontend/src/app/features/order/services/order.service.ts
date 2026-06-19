import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timeout } from 'rxjs';
import { CalculateShippingRequest, CreateInvoiceRequest, InvoiceResponse, InvoiceScreenResponse } from '../models/order.model';
import { DeliveryInfoRequest } from '../models/order.model';
import { CartItemRequest } from '../../cart/models/cart.model';
import { ApiResponse } from '../models/order.model'
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
/**
 * Coupling: Common/content coupling risk because checkout state is shared through mutable localStorage snapshots.
 * Cohesion: Related checkout concerns, but API access and client state persistence should be separated as it grows.
 */
export class OrderService {
  private http = inject(HttpClient);
  private invoiceStorageKey = 'aims_invoice';
  private deliveryStorageKey = 'aims_delivery';
  private currentOrderIdStorageKey = 'aims_current_order_id';

  private readonly BASE_URL = `${environment.apiUrl}/orders`;

  placeOrder(items: CartItemRequest[]): Observable<ApiResponse<string>> {
    return this.http
      .post<ApiResponse<string>>(`${this.BASE_URL}/place`, { items }, { withCredentials: true })
      .pipe(timeout(10000));
  }

  calculateShipping(deliveryInfo: DeliveryInfoRequest, items: CartItemRequest[]): Observable<ApiResponse<number>> {
    const request: CalculateShippingRequest = {
      deliveryProvince: deliveryInfo.deliveryProvince,
      items
    };
    return this.http
      .post<ApiResponse<number>>(`${this.BASE_URL}/calculate-shipping`, request)
      .pipe(timeout(10000));
  }

  createInvoice(deliveryInfo: DeliveryInfoRequest, items: CartItemRequest[]): Observable<ApiResponse<InvoiceResponse>> {
    const request: CreateInvoiceRequest = {
      deliveryInfo,
      items
    };
    return this.http
      .post<ApiResponse<InvoiceResponse>>(`${this.BASE_URL}/create-invoice`, request)
      .pipe(timeout(10000));
  }

  getInvoiceScreen(orderId: string): Observable<InvoiceScreenResponse> {
    return this.http
      .get<InvoiceScreenResponse>(`${this.BASE_URL}/${orderId}/invoice`)
      .pipe(timeout(10000));
  }

  confirmPaidOrder(orderId: string): Observable<ApiResponse<InvoiceResponse>> {
    return this.http
      .post<ApiResponse<InvoiceResponse>>(`${this.BASE_URL}/confirm-paid`, { orderId }, { withCredentials: true })
      .pipe(timeout(10000));
  }

  setCurrentOrderId(orderId: string): void {
    sessionStorage.setItem(this.currentOrderIdStorageKey, orderId);
  }

  getCurrentOrderId(): string | null {
    return sessionStorage.getItem(this.currentOrderIdStorageKey);
  }

  clearCheckoutState() {
    localStorage.removeItem(this.invoiceStorageKey);
    localStorage.removeItem(this.deliveryStorageKey);
    sessionStorage.removeItem(this.currentOrderIdStorageKey);
  }
}
