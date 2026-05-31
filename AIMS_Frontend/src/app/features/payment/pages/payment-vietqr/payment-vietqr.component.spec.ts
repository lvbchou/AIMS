import { TestBed } from '@vitest';
import { PaymentVietqrComponent } from './payment-vietqr.component';

describe('PaymentVietqrComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentVietqrComponent],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(PaymentVietqrComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
