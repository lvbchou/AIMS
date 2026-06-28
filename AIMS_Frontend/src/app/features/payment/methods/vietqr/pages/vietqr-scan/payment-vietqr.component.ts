// =============================================
// payment-vietqr.component.ts
// VietQR Payment screen with order summary
// =============================================

import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';

import { VietQRPaymentService } from '../../services/vietqr-payment.service';
import { Order, PaymentMethod } from '../../../../core/models/payment-core.model';

@Component({
  selector: 'app-payment-vietqr',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-vietqr.component.html',
  styleUrls: ['./payment-vietqr.component.scss'],
})
export class PaymentVietqrComponent implements OnInit, OnDestroy {
  order: Order | null = null;
  selectedMethod: PaymentMethod = 'BANK_TRANSFER';
  isLoading = true;
  isSubmitting = false;
  transactionId = '';

  // Added properties for the template
  qrError = false;
  emvQrCode = '';
  qrContent = ''; // Nội dung chuyển tiền (dùng cho Test Callback)
  qrImageUrl = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" viewBox="0 0 300 300">
      <rect width="300" height="300" fill="white"/>
      <rect x="18" y="18" width="264" height="264" fill="none" stroke="#0A4DA2" stroke-width="4"/>
      <g fill="#111111">
        <rect x="34" y="34" width="52" height="52"/>
        <rect x="42" y="42" width="36" height="36" fill="white"/>
        <rect x="50" y="50" width="20" height="20"/>

        <rect x="214" y="34" width="52" height="52"/>
        <rect x="222" y="42" width="36" height="36" fill="white"/>
        <rect x="230" y="50" width="20" height="20"/>

        <rect x="34" y="214" width="52" height="52"/>
        <rect x="42" y="222" width="36" height="36" fill="white"/>
        <rect x="50" y="230" width="20" height="20"/>

        <rect x="112" y="38" width="14" height="14"/>
        <rect x="136" y="38" width="14" height="14"/>
        <rect x="160" y="38" width="14" height="14"/>
        <rect x="184" y="38" width="14" height="14"/>

        <rect x="112" y="62" width="14" height="14"/>
        <rect x="136" y="62" width="14" height="14"/>
        <rect x="160" y="62" width="14" height="14"/>
        <rect x="184" y="62" width="14" height="14"/>

        <rect x="112" y="98" width="14" height="14"/>
        <rect x="136" y="98" width="14" height="14"/>
        <rect x="160" y="98" width="14" height="14"/>
        <rect x="184" y="98" width="14" height="14"/>

        <rect x="98" y="136" width="14" height="14"/>
        <rect x="122" y="136" width="14" height="14"/>
        <rect x="170" y="136" width="14" height="14"/>
        <rect x="194" y="136" width="14" height="14"/>

        <rect x="98" y="160" width="14" height="14"/>
        <rect x="122" y="160" width="14" height="14"/>
        <rect x="146" y="160" width="14" height="14"/>
        <rect x="170" y="160" width="14" height="14"/>
        <rect x="194" y="160" width="14" height="14"/>

        <rect x="112" y="184" width="14" height="14"/>
        <rect x="136" y="184" width="14" height="14"/>
        <rect x="160" y="184" width="14" height="14"/>
        <rect x="184" y="184" width="14" height="14"/>

        <rect x="112" y="218" width="14" height="14"/>
        <rect x="136" y="218" width="14" height="14"/>
        <rect x="160" y="218" width="14" height="14"/>
        <rect x="184" y="218" width="14" height="14"/>
      </g>
      <text x="150" y="282" font-family="Arial, sans-serif" font-size="13" font-weight="700" text-anchor="middle" fill="#0A4DA2">VietQR Mock</text>
    </svg>
  `)}`;
  paymentInfo = {
    provider: 'VietQR',
    orderId: '',
    totalPayable: 0
  };
  isUrgent = false;
  timerDisplay = '05:00';
  private timerInterval: any;

  payWithPayPal(): void {
    if (!this.order || this.isSubmitting) {
      this.router.navigate(['/payment/redirect'], { queryParams: { amount: this.paymentInfo.totalPayable } });
      return;
    }
    this.isSubmitting = true;
    this.router.navigate(['/payment/redirect'], {
      state: { orderId: this.order.id, amount: this.order.total },
    });
  }

  private destroy$ = new Subject<void>();

  readonly paymentMethods: { value: PaymentMethod; label: string; icon: string; description: string }[] = [
    {
      value: 'COD',
      label: 'Thanh toán khi nhận hàng',
      icon: 'payments',
      description: 'Trả tiền mặt khi nhận hàng',
    },
    {
      value: 'BANK_TRANSFER',
      label: 'Chuyển khoản ngân hàng',
      icon: 'account_balance',
      description: 'Qua VietQR hoặc internet banking',
    },
    {
      value: 'PAYPAL',
      label: 'PayPal',
      icon: 'language',
      description: 'Thanh toán qua tài khoản PayPal',
    },
  ];

  constructor(
    private vietQRPaymentService: VietQRPaymentService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
  ) { }

  ngOnInit(): void {
    this.startCountdown(10); // 5 minutes countdown
    // Get orderId dynamically from query parameters, e.g. /payment/vietqr?orderId=ORD-002
    // Fallback to ORD-1K if none provided.
    const orderId = this.route.snapshot.queryParamMap.get('orderId') || 'ORD-1K';

    // Dùng switchMap để flatten hai HTTP call thành một pipeline — tránh nested subscribe
    // gây zone.js detection issue khiến isLoading không cập nhật UI.
    this.vietQRPaymentService
      .getInvoiceScreen(orderId)
      .pipe(
        takeUntil(this.destroy$),
        tap((invoice) => {
          // Map invoice ngay khi nhận được, trước khi gọi QR
          this.order = {
            id: invoice.orderId,
            items: (invoice.lineItems || []).map((item: any) => ({
              id: item.productTitle,
              title: item.productTitle,
              type: 'CD' as const,
              price: item.unitSellingPrice,
              quantity: item.quantity,
              imageUrl: '',
              weight: 0,
            })),
            deliveryInfo: {
              recipientName: '',
              phone: '',
              email: '',
              province: '',
              address: '',
              deliveryType: 'STANDARD' as const,
              shippingFee: invoice.deliveryFee,
            },
            subtotal: invoice.totalProductPriceExclVat,
            vatAmount: invoice.totalProductPriceInclVat - invoice.totalProductPriceExclVat,
            total: invoice.totalAmountToBePaid,
          };
          this.paymentInfo.orderId = invoice.orderId;
          this.paymentInfo.totalPayable = invoice.totalAmountToBePaid;
        }),
        switchMap((invoice) =>
          this.vietQRPaymentService
            .generateVietQRCode({ orderId: invoice.orderId, totalPayable: invoice.totalAmountToBePaid } as any)
            .pipe(
              tap((qrResp) => {
                const emvQrCode = qrResp.qrCode || '';
                const amount = qrResp.amount || 0;
                const respOrderId = qrResp.orderId || orderId;

                if (emvQrCode) {
                  this.qrImageUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=300x300&data='
                    + encodeURIComponent(emvQrCode);
                } else if (qrResp.vietQrReference) {
                  this.qrImageUrl = qrResp.vietQrReference;
                }

                this.emvQrCode = emvQrCode;
                // Lưu content để dùng cho Test Callback (format: "Order ORD1K")
                this.qrContent = qrResp.content || '';
                this.paymentInfo.orderId = respOrderId;
                this.paymentInfo.totalPayable = amount;
                this.isLoading = false;
                // Buộc Angular re-render vì switchMap chạy ngoài zone
                this.cdr.detectChanges();
                console.log('QR loaded. isLoading:', this.isLoading, 'url:', this.qrImageUrl);
                console.log('[TestCallback] content:', this.qrContent, '| amount:', amount, '| bankAccount: 8823302684 | bankCode: 970418');

                // Gửi test callback qua VietQR Sandbox thực, không poll ngay
                const txnId = qrResp.transactionId;
                if (txnId) {
                  this.transactionId = txnId;
                }
              })
            )
        )
      )
      .subscribe({
        error: (err) => {
          console.error('Payment flow error:', err);
          this.qrError = true;
          this.isLoading = false;
          this.cdr.detectChanges();
          this.router.navigate(['/payment/failed'], {
            state: { reason: 'Không thể khởi tạo mã QR thanh toán từ VietQR.' }
          });
        },
      });
  }

  ngOnDestroy(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  selectMethod(method: PaymentMethod): void {
    this.selectedMethod = method;
  }

  confirmPayment(): void {
    if (!this.order || this.isSubmitting) {
      return;
    }

    if (this.timerDisplay === '00:00') {
      this.router.navigate(['/payment/failed'], { state: { result: 'Timeout' } });
      return;
    }

    this.isSubmitting = true;

    if (this.selectedMethod === 'BANK_TRANSFER') {
      this.router.navigate(['/payment/validating'], {
        state: {
          transactionId: this.transactionId || this.order.id,
          orderId: this.order.id,
          isVietQR: true
        }
      });
      return;
    }

    if (this.selectedMethod === 'PAYPAL') {
      this.router.navigate(['/payment/redirect'], {
        state: { orderId: this.order.id, amount: this.order.total },
      });
      return;
    }

    // COD is not handled in this screen — navigate to failed
    this.router.navigate(['/payment/failed'], {
      state: { reason: 'Unsupported payment method selected.' }
    });
    this.isSubmitting = false;
  }

  navigateBack(): void {
    this.router.navigate(['/']);
  }

  formatCurrency(amount: number): string {
    return `${amount.toLocaleString('vi-VN')} VND`;
  }

  onQrImageError(): void {
    // Nếu ảnh primary không load, thử fallback sang img.vietqr.io
    if (this.emvQrCode && !this.qrImageUrl.includes('img.vietqr.io')) {
      const safeId = this.paymentInfo.orderId.replace('-', '');
      this.qrImageUrl = 'https://img.vietqr.io/image/970418-8823302684-compact2.jpg'
        + '?amount=' + this.paymentInfo.totalPayable
        + '&addInfo=' + encodeURIComponent('Order ' + safeId)
        + '&accountName=' + encodeURIComponent('TESTAIMVD');
    } else {
      this.qrError = true;
    }
  }

  private startCountdown(seconds: number): void {
    let remaining = seconds;
    this.updateTimerDisplay(remaining);

    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }

    this.timerInterval = setInterval(() => {
      if (remaining > 0) {
        remaining--;
      } else {
        clearInterval(this.timerInterval);
        this.router.navigate(['/payment/failed'], {
          state: { result: 'Thời gian thanh toán qua mã QR đã hết hạn.' }
        });
      }
      this.isUrgent = remaining <= 60;
      this.updateTimerDisplay(remaining);
      this.cdr.detectChanges();
    }, 1000);
  }

  private updateTimerDisplay(totalSeconds: number): void {
    const minutes = Math.floor(totalSeconds / 60);
    const secs = totalSeconds % 60;
    this.timerDisplay = `${this.padZero(minutes)}:${this.padZero(secs)}`;
  }

  private padZero(num: number): string {
    return num < 10 ? `0${num}` : `${num}`;
  }

  get totalItems(): number {
    return this.order?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;
  }
}
