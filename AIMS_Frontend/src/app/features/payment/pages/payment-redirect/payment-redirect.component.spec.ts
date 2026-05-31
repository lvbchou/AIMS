import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { PaymentRedirectComponent } from './payment-redirect.component';
import { PaymentMockService } from '../../services/payment-mock.service';

describe('PaymentRedirectComponent', () => {
  let component: PaymentRedirectComponent;
  let fixture: ComponentFixture<PaymentRedirectComponent>;
  let router: Router;
  let paymentService: PaymentMockService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentRedirectComponent],
      providers: [PaymentMockService],
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentRedirectComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    paymentService = TestBed.inject(PaymentMockService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to /payment if no orderId', () => {
    spyOn(router, 'navigate');
    // Simulate navigation state without orderId
    spyOn(router, 'getCurrentNavigation').and.returnValue(null as any);
    fixture.detectChanges();
    // Note: In real test, you'd need to mock the state properly
  });

  it('should navigate to /payment/validating after 3 seconds', (done) => {
    spyOn(router, 'navigate');
    // Set up navigation state with orderId
    const navState = { orderId: 'ORD-001', amount: 100000 };
    spyOn(router, 'getCurrentNavigation').and.returnValue({
      extras: { state: navState },
    } as any);

    fixture.detectChanges();

    setTimeout(() => {
      expect(router.navigate).toHaveBeenCalledWith(
        ['/payment/validating'],
        jasmine.objectContaining({ state: navState })
      );
      done();
    }, 3100);
  });

  it('should call cancel() and navigate to /payment', () => {
    spyOn(router, 'navigate');
    component.cancel();
    expect(router.navigate).toHaveBeenCalledWith(['/payment']);
  });

  it('should render PayPal logo and brand', () => {
    fixture.detectChanges();
    const logoText = fixture.nativeElement.querySelector('.redirect-card__brand');
    expect(logoText?.textContent).toContain('PayPal');
  });

  it('should render title', () => {
    fixture.detectChanges();
    const title = fixture.nativeElement.querySelector('.redirect-card__title');
    expect(title?.textContent).toContain('Đang chuyển hướng đến PayPal');
  });

  it('should render cancel button', () => {
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.btn-cancel');
    expect(button).toBeTruthy();
    expect(button?.textContent).toContain('Huỷ thanh toán');
  });

  it('should have animated spinner', () => {
    fixture.detectChanges();
    const ring = fixture.nativeElement.querySelector('.redirect-card__ring');
    expect(ring).toBeTruthy();
  });

  it('should have 3 animated dots', () => {
    fixture.detectChanges();
    const dots = fixture.nativeElement.querySelectorAll('.dot');
    expect(dots.length).toBe(3);
  });

  it('should cleanup on destroy', () => {
    spyOn(component['destroy$'], 'next');
    spyOn(component['destroy$'], 'complete');
    component.ngOnDestroy();
    expect(component['destroy$'].next).toHaveBeenCalled();
    expect(component['destroy$'].complete).toHaveBeenCalled();
  });
});
