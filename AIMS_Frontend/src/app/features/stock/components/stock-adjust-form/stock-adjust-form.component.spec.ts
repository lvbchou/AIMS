import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StockAdjustFormComponent } from './stock-adjust-form.component';

describe('StockAdjustFormComponent', () => {
  let component: StockAdjustFormComponent;
  let fixture: ComponentFixture<StockAdjustFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StockAdjustFormComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(StockAdjustFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
