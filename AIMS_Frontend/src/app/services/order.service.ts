import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, timeout } from 'rxjs';

export interface DeliveryInfoRequest {
  recipientName: string;
  phoneNumber: string;
  email: string;
  deliveryProvince: string;
  detailAddress: string;
  note: string;
}

export interface CartItemRequest {
  productId: number;
  quantity: number;
}

export interface CartItemView {
  productId: number;
  title: string;
  category: string;
  unitPriceExVat: number;
  quantity: number;
  availableQuantity: number;
  imageUrl: string;
}

export interface InvoiceLineResponse {
  productId: number;
  title: string;
  category: string;
  image: string;
  quantity: number;
  unitPriceExVat: number;
  amountExVat: number;
}

export interface InvoiceResponse {
  invoiceId: string;
  orderId: string;
  issueDate: string;
  items: InvoiceLineResponse[];
  subtotalExVat: number;
  vat: number;
  subtotalIncVat: number;
  shippingFee: number;
  total: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
/**
 * Coupling: Common/content coupling risk because checkout state is shared through mutable localStorage snapshots.
 * Cohesion: Related checkout concerns, but API access and client state persistence should be separated as it grows.
 */
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/orders';
  private cartStorageKey = 'aims_cart';
  private invoiceStorageKey = 'aims_invoice';
  private deliveryStorageKey = 'aims_delivery';

  // Store the recent invoice and delivery info to pass to the invoice page
  private currentInvoiceSubject = new BehaviorSubject<InvoiceResponse | null>(this.loadInvoice());
  currentInvoice$ = this.currentInvoiceSubject.asObservable();

  private currentDeliveryInfoSubject = new BehaviorSubject<DeliveryInfoRequest | null>(this.loadDeliveryInfo());
  currentDeliveryInfo$ = this.currentDeliveryInfoSubject.asObservable();

  private currentCartSubject = new BehaviorSubject<CartItemView[]>(this.loadCart());
  currentCart$ = this.currentCartSubject.asObservable();

  placeOrder(items: CartItemRequest[]): Observable<ApiResponse<string>> {
    return this.http
      .post<ApiResponse<string>>(`${this.apiUrl}/place`, { items }, { withCredentials: true })
      .pipe(timeout(10000));
  }

  calculateShipping(deliveryInfo: DeliveryInfoRequest): Observable<ApiResponse<number>> {
    return this.http
      .post<ApiResponse<number>>(`${this.apiUrl}/calculate-shipping`, deliveryInfo, { withCredentials: true })
      .pipe(timeout(10000));
  }

  createInvoice(deliveryInfo: DeliveryInfoRequest): Observable<ApiResponse<InvoiceResponse>> {
    return this.http
      .post<ApiResponse<InvoiceResponse>>(`${this.apiUrl}/create-invoice`, deliveryInfo, { withCredentials: true })
      .pipe(timeout(10000));
  }

  confirmPaidOrder(orderId: string): Observable<ApiResponse<InvoiceResponse>> {
    return this.http
      .post<ApiResponse<InvoiceResponse>>(`${this.apiUrl}/confirm-paid`, { orderId }, { withCredentials: true })
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

  setCurrentCart(items: CartItemView[]) {
    localStorage.setItem(this.cartStorageKey, JSON.stringify(items));
    this.currentCartSubject.next(items);
  }

  getCurrentCart(): CartItemView[] {
    return this.currentCartSubject.value;
  }

  addToCart(item: CartItemView) {
    const currentCart = this.getCurrentCart();
    const existingItem = currentCart.find(cartItem => cartItem.productId === item.productId);
    const nextCart = existingItem
      ? currentCart.map(cartItem => cartItem.productId === item.productId
        ? {
            ...cartItem,
            availableQuantity: item.availableQuantity,
            quantity: Math.min(cartItem.quantity + item.quantity, item.availableQuantity)
          }
        : cartItem)
      : [...currentCart, item];
    this.setCurrentCart(nextCart);
  }

  clearCart() {
    localStorage.removeItem(this.cartStorageKey);
    this.currentCartSubject.next([]);
  }

  clearCheckoutState() {
    localStorage.removeItem(this.invoiceStorageKey);
    localStorage.removeItem(this.deliveryStorageKey);
    this.currentInvoiceSubject.next(null);
    this.currentDeliveryInfoSubject.next(null);
  }

  private loadCart(): CartItemView[] {
    const rawCart = localStorage.getItem(this.cartStorageKey);
    if (!rawCart) return [];

    try {
      const parsedCart = JSON.parse(rawCart) as CartItemView[];
      return parsedCart.map(item => ({
        ...item,
        availableQuantity: item.availableQuantity ?? item.quantity
      }));
    } catch {
      localStorage.removeItem(this.cartStorageKey);
      return [];
    }
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
