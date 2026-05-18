import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StockTableComponent } from './stock-table.component';

describe('StockTableComponent', () => {
  let component: StockTableComponent;
  let fixture: ComponentFixture<StockTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StockTableComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(StockTableComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
