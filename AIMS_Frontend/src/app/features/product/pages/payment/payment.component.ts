import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PaymentService } from '../../../../core/services/payment.service';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.scss'
})
export class PaymentComponent implements OnInit, OnDestroy {
  orderId = '161';
  totalPayable = 126500; // 126,500 VND
  timeLeft = 600; // 10 minutes in seconds
  formattedTime = '10:00';
  private timerSubscription?: Subscription;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.startTimer();
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
    // Navigate to the intermediate "Redirecting to PayPal..." screen
    this.router.navigate(['/payment/redirect'], { queryParams: { amount: this.totalPayable } });
  }

  onConfirmVietQR(): void {
    alert('VietQR payment method is processed by another team member.');
  }
}
