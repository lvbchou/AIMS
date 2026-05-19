import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerLayoutComponent } from './customer-layout.component';

describe('CustomerLayoutComponent', () => {
  let component: CustomerLayoutComponent;
  let fixture: ComponentFixture<CustomerLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomerLayoutComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CustomerLayoutComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
