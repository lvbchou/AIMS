import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import {
  Invoice,
  PaymentStatusResponse,
  QRCodeResponse,
} from '../models/payment.model';

@Injectable({
  providedIn: 'root',
})
export class VietQRPaymentService {
  private apiBaseUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Generate QR Code for payment through the AIMS backend.
   */
  generateVietQRCode(invoice: Invoice): Observable<QRCodeResponse> {
    const qrUrl = `${this.apiBaseUrl}/orders/${invoice.orderId}/pay/vietqr/qrcode`;

    return this.http.post<any>(qrUrl, {}).pipe(
      map((response) => ({
        qrCode: response?.qrCodeImageBase64 || response?.qrCode || response?.qrDataUrl || '',
        qrDataUrl: response?.qrCodeImageBase64 || response?.qrCode || response?.qrDataUrl || '',
        vietQrReference: response?.vietQrReference || '',
        transactionId: response?.transactionId || invoice.orderId,
        amount: response?.totalAmountToBePaid || invoice.totalPayable,
        orderId: response?.orderId || invoice.orderId,
        // N\u1ed9i dung chuy\u1ec3n ti\u1ec1n — b\u1ea7t bu\u1ed9c \u0111\u1ec3 g\u1ecdi Test Callback \u0111\u00fang c\u00e1ch
        content: response?.content || '',
        accountName: environment.vietqr.accountName,
        accountNumber: environment.vietqr.accountNumber,
      })),
      catchError((error) => {
        console.error('QR Generation Error:', error);
        return throwError(
          () =>
            new Error(
              `QR Code generation failed: ${error?.error?.message || error.message}`
            )
        );
      })
    );
  }

  /**
   * Trigger VietQR test callback via backend. Backend will call
   * dev.vietqr.org/vqr/bank/api/test/transaction-callback with correct auth,
   * and VietQR will then call our transaction-sync endpoint to update the DB.
   */
  triggerTestCallback(orderId: string): Observable<any> {
    const url = `${this.apiBaseUrl}/orders/${orderId}/pay/vietqr/test-callback`;
    return this.http.post<any>(url, {});
  }

  /**
   * Check payment status via the AIMS backend.
   */
  checkPaymentStatus(transactionId: string): Observable<PaymentStatusResponse> {
    const statusUrl = `${this.apiBaseUrl}/payment/status/${transactionId}`;

    return this.http.get<any>(statusUrl).pipe(
      map((response): PaymentStatusResponse => ({
        transactionId,
        orderId: transactionId,
        status: response?.success ? 'COMPLETED' : 'PENDING',
        amount: 0,
        timestamp: new Date().toISOString(),
        message: response?.success ? 'Payment processed' : 'Payment pending',
      })),
      catchError((error) => {
        console.error('Payment Status Check Error:', error);
        return throwError(
          () =>
            new Error(
              `Payment status check failed: ${error?.error?.message || error.message}`
            )
        );
      })
    );
  }

  /**
   * Check payment status by orderId (uses order-level status endpoint).
   */
  checkPaymentStatusByOrderId(orderId: string): Observable<PaymentStatusResponse> {
    const statusUrl = `${this.apiBaseUrl}/orders/${orderId}/pay/status`;

    return this.http.get<any>(statusUrl).pipe(
      map((response): PaymentStatusResponse => ({
        transactionId: response?.transactionId || orderId,
        orderId,
        status: response?.success ? 'COMPLETED' : 'PENDING',
        amount: 0,
        timestamp: new Date().toISOString(),
        message: response?.success ? 'Payment processed' : 'Payment pending',
      })),
      catchError(() =>
        // Fallback: not found = still pending
        of({
          transactionId: orderId,
          orderId,
          status: 'PENDING' as const,
          amount: 0,
          timestamp: new Date().toISOString(),
          message: 'Payment pending',
        })
      )
    );
  }

  /**
   * Poll payment status by orderId — more reliable than transactionId polling
   * because it works even if transactionId was not captured.
   */
  pollByOrderId(
    orderId: string,
    timeoutMs = environment.payment.timeout,
    intervalMs = environment.payment.pollInterval
  ): Observable<PaymentStatusResponse> {
    return new Observable((observer) => {
      const startTime = Date.now();
      let pollCount = 0;

      const pollFn = () => {
        if (Date.now() - startTime > timeoutMs) {
          observer.error(new Error('Payment polling timeout'));
          return;
        }

        pollCount++;
        this.checkPaymentStatusByOrderId(orderId).subscribe({
          next: (status) => {
            console.log(`[Poll #${pollCount}] OrderId=${orderId} Status:`, status.status);
            if (status.status === 'COMPLETED') {
              observer.next(status);
              observer.complete();
            } else {
              setTimeout(pollFn, intervalMs);
            }
          },
          error: () => setTimeout(pollFn, intervalMs),
        });
      };

      pollFn();
    });
  }

  /**
   * Poll payment status until timeout or success
   * @param transactionId - VietQR transaction ID
   * @param timeoutMs - Maximum polling time (default: 5 minutes)
   * @param intervalMs - Polling interval (default: 5 seconds)
   */
  pollPaymentStatus(
    transactionId: string,
    timeoutMs = environment.payment.timeout,
    intervalMs = environment.payment.pollInterval
  ): Observable<PaymentStatusResponse> {
    return new Observable((observer) => {
      let startTime = Date.now();
      let pollCount = 0;

      const pollFn = () => {
        if (Date.now() - startTime > timeoutMs) {
          observer.error(new Error('Payment polling timeout'));
          return;
        }

        pollCount++;
        this.checkPaymentStatus(transactionId).subscribe({
          next: (status) => {
            console.log(`[Poll #${pollCount}] Status:`, status.status);

            if (status.status === 'COMPLETED') {
              observer.next(status);
              observer.complete();
            } else if (status.status === 'FAILED') {
              observer.error(new Error(`Payment failed: ${status.message}`));
            } else {
              // PENDING - continue polling
              setTimeout(pollFn, intervalMs);
            }
          },
          error: (err) => {
            // Retry on error until timeout
            console.log(`[Poll #${pollCount}] Error:`, err.message);
            setTimeout(pollFn, intervalMs);
          },
        });
      };

      pollFn();
    });
  }

  /**
   * Normalize VietQR status to standard payment status
   */
  private normalizeStatus(vietqrStatus: string): string {
    const statusMap: { [key: string]: string } = {
      COMPLETED: 'COMPLETED',
      SUCCESS: 'COMPLETED',
      PAID: 'COMPLETED',
      PENDING: 'PENDING',
      UNPAID: 'PENDING',
      FAILED: 'FAILED',
      ERROR: 'FAILED',
      CANCELLED: 'FAILED',
    };
    return statusMap[vietqrStatus?.toUpperCase()] || 'PENDING';
  }

  /**
   * Get current access token
   */
  getAccessToken(): Observable<string | null> {
    return throwError(() => new Error('Token flow is disabled. Use backend QR generation instead.'));
  }

  /**
   * Verify webhook callback from VietQR (backend should call this)
   */
  verifyWebhookSignature(payload: any, signature: string): boolean {
    // Implement HMAC-SHA256 verification with clientSecret
    // This should be done on backend for security
    console.warn(
      'Webhook verification should be done on backend, not frontend'
    );
    return false; // Always return false for frontend
  }

  /**
   * Get invoice details for payment screen.
   */
  getInvoiceScreen(orderId: string): Observable<any> {
    const url = `${this.apiBaseUrl}/orders/${orderId}/pay/invoice`;
    return this.http.get<any>(url).pipe(
      catchError((error) => {
        console.error('Get Invoice Error:', error);
        return throwError(
          () => new Error(`Failed to load invoice: ${error?.error?.message || error.message}`)
        );
      })
    );
  }

  /**
   * Get order confirmation details after successful payment.
   */
  getOrderConfirmation(orderId: string): Observable<any> {
    const url = `${this.apiBaseUrl}/orders/${orderId}/pay/confirmation`;
    return this.http.get<any>(url).pipe(
      catchError((error) => {
        console.error('Get Confirmation Error:', error);
        return throwError(
          () => new Error(`Failed to load order confirmation: ${error?.error?.message || error.message}`)
        );
      })
    );
  }
}
