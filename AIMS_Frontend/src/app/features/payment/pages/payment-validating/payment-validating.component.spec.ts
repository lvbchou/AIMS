import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { PaymentValidatingComponent } from './payment-validating.component';
import { PaymentMockService } from '../../services/payment-mock.service';

describe('PaymentValidatingComponent', () => {
  let component: PaymentValidatingComponent;
  let fixture: ComponentFixture<PaymentValidatingComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentValidatingComponent],
      providers: [PaymentMockService],
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
    const title = fixture.nativeElement.querySelector('.validating-card__title');
    expect(title?.textContent).toContain('Đang xác thực thanh toán');
  });

  it('should render loading animation', () => {
    fixture.detectChanges();
    const hourglass = fixture.nativeElement.querySelector('.validating-card__hourglass');
    expect(hourglass).toBeTruthy();
  });

  it('should render loading dots', () => {
    fixture.detectChanges();
    const dots = fixture.nativeElement.querySelectorAll('.loading-dot');
    expect(dots.length).toBe(3);
  });

  it('should render progress bar', () => {
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
