import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductType } from '../../models/product-type.enum';
import { ProductService } from '../../services/product.service';
import { MOCK_DETAIL } from '../../data/mock-products.data';

@Component({
  selector: 'app-product-detail-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-detail-modal.component.html',
  styleUrls: ['./product-detail-modal.component.scss']
})
export class ProductDetailModalComponent implements OnInit, OnDestroy {
  @Input() productId!: number;
  @Output() close     = new EventEmitter<void>();
  @Output() addToCart = new EventEmitter<{ product: any; quantity: number }>();

  product: any = null;
  loading = true;
  quantity = 1;
  readonly ProductType = ProductType;

  constructor(private api: ProductService) {}

  ngOnInit(): void {
    document.body.style.overflow = 'hidden';

    // Hiện mock ngay để UX nhanh
    this.product = MOCK_DETAIL[this.productId] ?? null;
    this.loading = !this.product;

    // Gọi API lấy data thật — SD ViewProductDetails step 1.1.1
    this.api.getById(this.productId).subscribe({
      next: (p: any) => { this.product = p; this.loading = false; },
      error: ()      => { this.loading = false; } // fallback về mock
    });
  }

  ngOnDestroy(): void { document.body.style.overflow = ''; }

  onClose(): void { this.close.emit(); }

  onAddToCart(): void {
    if (this.product) {
      this.addToCart.emit({ product: this.product, quantity: this.quantity });
      this.onClose();
    }
  }

  decrement(): void { if (this.quantity > 1) this.quantity--; }
  increment(): void { this.quantity++; }
  fmt(n: number): string { return n.toLocaleString('vi-VN') + ' VND'; }

  get type(): ProductType     { return this.product?.productType; }
  get d(): any                { return this.product?.typeDetails; }
  get isBook():      boolean  { return this.type === ProductType.BOOK; }
  get isCd():        boolean  { return this.type === ProductType.CD; }
  get isDvd():       boolean  { return this.type === ProductType.DVD; }
  get isNewspaper(): boolean  { return this.type === ProductType.NEWSPAPER; }

  get typeLabel(): string { return String(this.type ?? '').toUpperCase(); }
  get typeClass(): string {
    switch (this.type) {
      case ProductType.BOOK:      return 'badge-book';
      case ProductType.CD:        return 'badge-cd';
      case ProductType.DVD:       return 'badge-dvd';
      case ProductType.NEWSPAPER: return 'badge-newspaper';
      default:                    return '';
    }
  }
}