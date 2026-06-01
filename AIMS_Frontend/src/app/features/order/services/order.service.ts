import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, timeout } from 'rxjs';
import { InvoiceResponse } from '../models/order.model';
import { DeliveryInfoRequest } from '../models/order.model';
import { CartItemView } from '../../cart/models/cart.model';
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

  private readonly BASE_URL = `${environment.apiUrl}/orders`;

  // Store the recent invoice and delivery info to pass to the invoice page
  private currentInvoiceSubject = new BehaviorSubject<InvoiceResponse | null>(this.loadInvoice());
  currentInvoice$ = this.currentInvoiceSubject.asObservable();

  private currentDeliveryInfoSubject = new BehaviorSubject<DeliveryInfoRequest | null>(this.loadDeliveryInfo());
  currentDeliveryInfo$ = this.currentDeliveryInfoSubject.asObservable();

  placeOrder(items: CartItemRequest[]): Observable<ApiResponse<string>> {
    return this.http
      .post<ApiResponse<string>>(`${this.BASE_URL}/place`, { items }, { withCredentials: true })
      .pipe(timeout(10000));
  }

  calculateShipping(deliveryInfo: DeliveryInfoRequest): Observable<ApiResponse<number>> {
    return this.http
      .post<ApiResponse<number>>(`${this.BASE_URL}/calculate-shipping`, deliveryInfo, { withCredentials: true })
      .pipe(timeout(10000));
  }

  createInvoice(deliveryInfo: DeliveryInfoRequest): Observable<ApiResponse<InvoiceResponse>> {
    return this.http
      .post<ApiResponse<InvoiceResponse>>(`${this.BASE_URL}/create-invoice`, deliveryInfo, { withCredentials: true })
      .pipe(timeout(10000));
  }

  confirmPaidOrder(orderId: string): Observable<ApiResponse<InvoiceResponse>> {
    return this.http
      .post<ApiResponse<InvoiceResponse>>(`${this.BASE_URL}/confirm-paid`, { orderId }, { withCredentials: true })
      .pipe(timeout(10000));
  }

  setCurrentInvoice(invoice: InvoiceResponse) {
    localStorage.setItem(this.invoiceStorageKey, JSON.stringify(invoice));
    this.currentInvoiceSubject.next(invoice);
  }

  setCurrentDeliveryInfo(info: DeliveryInfoRequest) {
    localStorage.setItem(this.deliveryStorageKey, JSON.stringify(info));
    this.currentDeliveryInfoSubject.next(info);
  }

  getCurrentInvoice(): InvoiceResponse | null {
    return this.currentInvoiceSubject.value;
  }

  getCurrentDeliveryInfo(): DeliveryInfoRequest | null {
    return this.currentDeliveryInfoSubject.value;
  }

  clearCheckoutState() {
    localStorage.removeItem(this.invoiceStorageKey);
    localStorage.removeItem(this.deliveryStorageKey);
    this.currentInvoiceSubject.next(null);
    this.currentDeliveryInfoSubject.next(null);
  }

  private loadInvoice(): InvoiceResponse | null {
    const rawInvoice = localStorage.getItem(this.invoiceStorageKey);
    if (!rawInvoice) return null;

    try {
      return JSON.parse(rawInvoice) as InvoiceResponse;
    } catch {
      localStorage.removeItem(this.invoiceStorageKey);
      return null;
    }
  }

  private loadDeliveryInfo(): DeliveryInfoRequest | null {
    const rawDeliveryInfo = localStorage.getItem(this.deliveryStorageKey);
    if (!rawDeliveryInfo) return null;

    try {
      return JSON.parse(rawDeliveryInfo) as DeliveryInfoRequest;
    } catch {
      localStorage.removeItem(this.deliveryStorageKey);
      return null;
    }
  }
}
