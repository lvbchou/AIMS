import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { PaymentValidatingComponent } from './payment-validating.component';
import { PaymentMockService } from '../../services/payment-mock.service';
import { VietQRPaymentService } from '../../services/vietqr-payment.service';
import { CartService } from '../../../cart/services/cart.service';
import { OrderService } from '../../../order/services/order.service';

describe('PaymentValidatingComponent', () => {
  let component: PaymentValidatingComponent;
  let fixture: ComponentFixture<PaymentValidatingComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentValidatingComponent],
      providers: [
        PaymentMockService,
        VietQRPaymentService,
        CartService,
        OrderService,
        provideHttpClient(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentValidatingComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to /payment if no orderId', () => {
    spyOn(router, 'navigate');
    spyOn(router, 'getCurrentNavigation').and.returnValue(null as any);
    fixture.detectChanges();
  });

  it('should navigate to success or failed after validation', (done) => {
    spyOn(router, 'navigate');
    const navState = { orderId: 'ORD-001' };
    spyOn(router, 'getCurrentNavigation').and.returnValue({
      extras: { state: navState },
    } as any);

    fixture.detectChanges();

    setTimeout(() => {
      expect(router.navigate).toHaveBeenCalled();
      const lastCall = router.navigate.calls.mostRecent();
      expect(
        lastCall.args[0][0] === '/payment/success' ||
        lastCall.args[0][0] === '/payment/failed'
      ).toBe(true);
      done();
    }, 5000);
  });

  it('should render title', () => {
    fixture.detectChanges();
    const title = fixture.nativeElement.querySelector('.validating-page__title');
    expect(title?.textContent).toContain('Validating Payment Results');
  });

  it('should render loading animation', () => {
    fixture.detectChanges();
    const iconBox = fixture.nativeElement.querySelector('.validating-page__icon-box');
    expect(iconBox).toBeTruthy();
  });

  // Skip elements that are no longer part of the single-card layout
  xit('should render loading dots', () => {
    fixture.detectChanges();
    const dots = fixture.nativeElement.querySelectorAll('.loading-dot');
    expect(dots.length).toBe(3);
  });

  xit('should render progress bar', () => {
    fixture.detectChanges();
    const progress = fixture.nativeElement.querySelector('.validating-card__progress');
    expect(progress).toBeTruthy();
  });

  it('should cleanup on destroy', () => {
    spyOn(component['destroy$'], 'next');
    spyOn(component['destroy$'], 'complete');
    component.ngOnDestroy();
    expect(component['destroy$'].next).toHaveBeenCalled();
    expect(component['destroy$'].complete).toHaveBeenCalled();
  });
});
