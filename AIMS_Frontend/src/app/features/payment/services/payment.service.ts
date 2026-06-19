import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface PaymentInitiateResponse {
  approvalUrl: string;
  orderId: string;
  transactionId?: string;
}

export interface PaymentCompleteResponse {
  status: string;
  message: string;
  orderId?: string;
  transactionId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly BASE_URL = `${environment.apiUrl}/payment`;

  constructor(private http: HttpClient) {}

  /**
   * Calls backend POST /api/payment/initiate to start a payment session.
   * Returns the approval checkout redirect URL and the local Order ID.
   */
  initiatePayment(amount: number, orderId: string): Observable<PaymentInitiateResponse> {
    return this.http.post<PaymentInitiateResponse>(`${this.BASE_URL}/initiate`, { amount, orderId });
  }

  /**
   * Calls backend POST /api/payment/complete to capture the payment on success redirect.
   */
  completePayment(token: string): Observable<PaymentCompleteResponse> {
    return this.http.post<PaymentCompleteResponse>(`${this.BASE_URL}/complete`, { token });
  }
}
