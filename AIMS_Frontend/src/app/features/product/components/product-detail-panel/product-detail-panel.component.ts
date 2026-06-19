import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Product } from '../../models/product.model';
import { ProductDetailBuilderService } from '../../services/detail-builders/product-detail-builder.service';
import { ProductDetailViewModel } from '../../services/detail-builders/product-type-detail-builder.interface';

/**
 * ProductDetailPanelComponent — pure display, không biết về context.
 *
 * SRP  : chỉ render thông tin sản phẩm.
 * OCP  : không biết gì về ProductType — delegate hoàn toàn sang builder.
 * ISP  : không inject CartService, ToastService, hay bất kỳ action service nào.
 *
 * Dùng ng-content slots để parent inject phần khác nhau:
 *   [slot=price]  — customer: 1 giá / manager: 2 giá
 *   [slot=action] — customer: cart button / manager: không có
 */
@Component({
  selector: 'app-product-detail-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-detail-panel.component.html',
  styleUrls: ['./product-detail-panel.component.scss'],
})
export class ProductDetailPanelComponent implements OnChanges {
  @Input() product!: Product;

  vm!: ProductDetailViewModel;

  constructor(private builder: ProductDetailBuilderService) {}

  ngOnChanges(): void {
    if (this.product) {
      this.vm = this.builder.build(this.product);
    }
  }
}