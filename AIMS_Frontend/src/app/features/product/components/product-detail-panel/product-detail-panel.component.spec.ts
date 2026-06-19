import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductDetailPanelComponent } from './product-detail-panel.component';

describe('ProductDetailPanelComponent', () => {
  let component: ProductDetailPanelComponent;
  let fixture: ComponentFixture<ProductDetailPanelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDetailPanelComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductDetailPanelComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
