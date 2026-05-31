import { TestBed } from '@vitest';
import { PaymentSuccessComponent } from './payment-success.component';

describe('PaymentSuccessComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentSuccessComponent],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(PaymentSuccessComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
