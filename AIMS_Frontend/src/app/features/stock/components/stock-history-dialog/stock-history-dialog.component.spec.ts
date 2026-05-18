import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StockHistoryDialogComponent } from './stock-history-dialog.component';

describe('StockHistoryDialogComponent', () => {
  let component: StockHistoryDialogComponent;
  let fixture: ComponentFixture<StockHistoryDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StockHistoryDialogComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(StockHistoryDialogComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
