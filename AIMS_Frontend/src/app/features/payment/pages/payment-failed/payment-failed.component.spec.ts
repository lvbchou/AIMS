import { TestBed } from '@vitest';
import { PaymentFailedComponent } from './payment-failed.component';

describe('PaymentFailedComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentFailedComponent],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(PaymentFailedComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
