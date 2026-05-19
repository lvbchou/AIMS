import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HeaderCustomerComponent } from './header-customer.component';

describe('HeaderCustomerComponent', () => {
  let component: HeaderCustomerComponent;
  let fixture: ComponentFixture<HeaderCustomerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderCustomerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderCustomerComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
