// =============================================
// payment-mock.service.ts
// Mock service for Pay Order use case
// =============================================

import { Injectable } from '@angular/core';
import { Observable, of, throwError, timer } from 'rxjs';
import { delay, switchMap } from 'rxjs/operators';
import {
  Order,
  OrderItem,
  DeliveryInfo,
  PaymentRequest,
  PaymentResult,
  PayPalRedirectInfo,
  PaymentMethod,
  QRCodeResponse,
  Invoice,
  VietQRPaymentInfo,
} from '../models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentMockService {

  // ── Mock Data ────────────────────────────────────────────────
  private mockOrder: Order = {
    id: 'ORD-001',
    items: [
      {
        id: 'cd-001',
        title: 'Rush (Deluxe Edition)',
        type: 'CD',
        price: 229_000,
        quantity: 2,
        imageUrl: 'assets/images/mock-cd.png',
        weight: 0.3,
      },
      {
        id: 'dvd-002',
        title: 'The Dark Knight (Blu-ray)',
        type: 'DVD',
        price: 189_000,
        quantity: 1,
        imageUrl: 'assets/images/mock-dvd.png',
        weight: 0.2,
      },
    ],
    deliveryInfo: {
      recipientName: 'Nguyễn Văn An',
      phone: '0945 647 788',
      email: 'nguyenvanan@example.com',
      province: 'Hà Nội',
      address: '123 Phố Huế, Quận Hai Bà Trưng',
      deliveryType: 'STANDARD',
      shippingFee: 25_000,
    },
    subtotal: 647_000,
    vatAmount: 64_700,
    total: 736_700,
  };

  // ── Getters ──────────────────────────────────────────────────

  /** Retrieve current pending order */
  getCurrentOrder(): Observable<Order> {
    return of(this.mockOrder).pipe(delay(300));
  }

  /** Get PayPal redirect URL */
  getPayPalRedirectInfo(orderId: string): Observable<PayPalRedirectInfo> {
    return of({
      paypalUrl: 'https://www.sandbox.paypal.com/checkoutnow?token=MOCK_TOKEN',
      returnUrl: `${window.location.origin}/payment/validating`,
      cancelUrl: `${window.location.origin}/payment`,
      orderId,
    }).pipe(delay(800));
  }

  /** Generate VietQR Code (Mock) */
  generateVietQRCode(invoice: Invoice): Observable<QRCodeResponse> {
    // Mock QR SVG data (simple test QR code)
    const mockQRSvg = `data:image/svg+xml;base64,${btoa(`
      <svg width="300" height="300" xmlns="http://www.w3.org/2000/svg">
        <rect width="300" height="300" fill="white"/>
        <g fill="black">
          ${Array.from({length: 15}, (_, i) => 
            `<rect x="${i * 20}" y="20" width="15" height="15"/>`
          ).join('')}
          ${Array.from({length: 12}, (_, i) => 
            `<rect x="20" y="${20 + (i+1) * 20}" width="15" height="15"/>`
          ).join('')}
        </g>
        <text x="150" y="280" font-size="12" text-anchor="middle" fill="black">
          Mock QR Code
        </text>
      </svg>
    `)}`;

    return of<QRCodeResponse>({
      qrCode: mockQRSvg,
      qrDataUrl: mockQRSvg,
      transactionId: `MOCK-${Date.now()}`,
      amount: invoice.totalPayable,
      orderId: invoice.orderId,
      accountName: 'AIMS Store',
      accountNumber: '1234567890',
      paymentInfo: {
        orderId: invoice.orderId,
        provider: 'VietQR (Mock)',
        totalPayable: invoice.totalPayable,
        description: `Payment for Order ${invoice.orderId}`,
        expirationTime: 300
      }
    }).pipe(delay(600));
  }

  /** Get Payment Confirmation (Mock) */
  getPaymentConfirmation(orderId: string): Observable<any> {
    return of({
      customerName: 'Nguyễn Văn An',
      phoneNumber: '0945 647 788',
      shippingAddress: '123 Phố Huế, Quận Hai Bà Trưng',
      province: 'Hà Nội',
      totalAmount: 736_700,
      transactionId: `TXN-${Date.now()}`,
      transactionContent: `Payment for Order ${orderId}`,
      transactionDatetime: new Date().toLocaleString('vi-VN'),
      orderReference: `AIMS-${orderId}-SUC`
    }).pipe(delay(500));
  }

  /** Get Payment Failure (Mock) */
  getPaymentFailure(orderId: string): Observable<any> {
    return of({
      errorCode: 'PAYMENT_TIMEOUT',
      errorMessage: 'Payment QR code expired. Please try again.',
      orderId: orderId
    }).pipe(delay(500));
  }

  /** Validate payment status after PayPal redirect */
  validatePayment(transactionId: string): Observable<any> {
    return of({
      success: true,
      transactionId: transactionId,
      orderReference: `AIMS-${transactionId}-SUC`
    }).pipe(delay(2500));
  }

  // ── Payment Actions ──────────────────────────────────────────

  /**
   * Process payment. Simulates:
   *  - COD / Bank Transfer → always SUCCESS after 1.2 s
   *  - PayPal              → called after redirect; 80% SUCCESS, 20% FAILED
   */
  processPayment(req: PaymentRequest): Observable<PaymentResult> {
    const latency = req.method === 'PAYPAL' ? 2500 : 1200;

    return timer(latency).pipe(
      switchMap(() => {
        const succeed =
          req.method !== 'PAYPAL' ? true : Math.random() > 0.2;

        if (succeed) {
          return of<PaymentResult>({
            transactionId: `TXN-${Date.now()}`,
            status: 'SUCCESS',
            message: 'Thanh toán thành công.',
            timestamp: new Date(),
            orderId: req.orderId,
          });
        }

        return throwError(() => ({
          transactionId: `TXN-${Date.now()}`,
          status: 'FAILED',
          message: 'Thanh toán thất bại. Vui lòng thử lại.',
          timestamp: new Date(),
          orderId: req.orderId,
        }));
      })
    );
  }

  // ── Helpers ──────────────────────────────────────────────────

  /** Format VND currency per spec: "229,000 VND" */
  formatCurrency(amount: number): string {
    return `${amount.toLocaleString('vi-VN')} VND`;
  }

  /** Format date per spec: "HH:mm:ss DD/MM/YYYY" */
  formatDateTime(date: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    return (
      `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())} ` +
      `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()}`
    );
  }
}