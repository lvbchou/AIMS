import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductType } from '../../models/product-type.enum';
import { ProductService } from '../../services/product.service';
import { ChangeDetectorRef } from '@angular/core';  
import { MOCK_DETAIL } from '../../data/mock-products.data';
import { Product } from '../../models/product.model';
import { CartService } from '../../../cart/services/cart.service';
import { ToastService } from '../../../../core/services/toast/toast.service';

@Component({
  selector: 'app-product-detail-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-detail-modal.component.html',
  styleUrls: ['./product-detail-modal.component.scss']
})
export class ProductDetailModalComponent implements OnInit, OnDestroy {
  @Input() product!: Product;
  @Output() close      = new EventEmitter<void>();
  @Output() addToCart  = new EventEmitter<{ product: Product; quantity: number }>();

  loading = true;
  quantity = 1;
  readonly ProductType = ProductType;

  constructor(
    private cartService: CartService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef,
  ){}

  ngOnInit(): void {
    document.body.style.overflow = 'hidden';
    if (this.product) {
      this.loading = false;
    }
  }

  ngOnDestroy(): void { document.body.style.overflow = ''; }

  onClose(): void { this.close.emit(); }
  onAddToCart(): void {
    this.cartService.addToCart(this.product.productId, this.quantity);
    this.toastService.show('Added to cart');
    this.onClose();
  }
  decrement(): void { if (this.quantity > 1) this.quantity--; }
  increment(): void { this.quantity++; }
  fmt(n: number): string { return n.toLocaleString('vi-VN') + ' VND'; }

  get type(): ProductType | null {
    return this.product?.productType ?? null;
  }
  get d(): any { return this.product?.typeDetails; }
  get isBook():      boolean { return this.type === ProductType.BOOK; }
  get isCd():        boolean { return this.type === ProductType.CD; }
  get isDvd():       boolean { return this.type === ProductType.DVD; }
  get isNewspaper(): boolean { return this.type === ProductType.NEWSPAPER; }

  get typeLabel(): string { return String(this.type ?? '').toUpperCase(); }
  get typeClass(): string {
    switch (this.type) {
      case ProductType.BOOK:      return 'badge-book';
      case ProductType.CD:        return 'badge-cd';
      case ProductType.DVD:       return 'badge-dvd';
      case ProductType.NEWSPAPER: return 'badge-newspaper';
      default: return '';
    }
  }
}
