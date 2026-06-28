import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { OrderService } from '../../../../order/services/order.service';

@Component({
  selector: 'app-payment-gateway',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-gateway.component.html',
  styleUrl: './payment-gateway.component.scss'
})
export class PaymentGatewayComponent implements OnInit, OnDestroy {
  orderId = '';
  totalPayable = 0;
  timeLeft = 600; // 10 minutes in seconds
  formattedTime = '10:00';
  private timerSubscription?: Subscription;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.startTimer();
    this.route.queryParamMap.subscribe(params => {
      const qOrderId = params.get('orderId') || this.orderService.getCurrentOrderId();
      if (qOrderId) {
        this.orderId = qOrderId;
        this.orderService.getInvoiceScreen(qOrderId).subscribe({
          next: (inv) => {
            this.totalPayable = inv.totalAmountToBePaid;
          },
          error: (err) => {
            console.error('Failed to load invoice details in PaymentComponent:', err);
          }
        });
      }
    });
  }

  ngOnDestroy(): void {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  startTimer(): void {
    this.timerSubscription = interval(1000).subscribe(() => {
      if (this.timeLeft > 0) {
        this.timeLeft--;
        const mins = Math.floor(this.timeLeft / 60);
        const secs = this.timeLeft % 60;
        this.formattedTime = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
      } else {
        this.formattedTime = 'Expired';
        if (this.timerSubscription) {
          this.timerSubscription.unsubscribe();
        }
      }
    });
  }

  onPayWithPayPal(): void {
    this.router.navigate(['/payment/redirect'], { 
      queryParams: { 
        orderId: this.orderId, 
        amount: this.totalPayable 
      } 
    });
  }

  onConfirmVietQR(): void {
    this.router.navigate(['/payment/vietqr'], { 
      queryParams: { 
        orderId: this.orderId 
      } 
    });
  }
}
