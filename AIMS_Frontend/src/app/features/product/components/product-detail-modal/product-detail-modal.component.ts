import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Product } from '../../models/product.model';
import { ProductDetailPanelComponent } from '../product-detail-panel/product-detail-panel.component';
import { CartService } from '../../../cart/services/cart.service';
import { ToastService } from '../../../../core/services/toast/toast.service';

/**
 * ProductDetailModalComponent — customer shell.
 *
 * Chỉ chứa:
 *   - Modal lifecycle (scroll lock)
 *   - Cart action (quantity + add to cart)
 *   - Price slot: selling price only
 *
 * Không biết gì về ProductType, typeDetails, hay display logic.
 * Tất cả display delegate sang ProductDetailPanelComponent.
 */
@Component({
  selector: 'app-product-detail-modal',
  standalone: true,
  imports: [CommonModule, ProductDetailPanelComponent],
  templateUrl: './product-detail-modal.component.html',
  styleUrls: ['./product-detail-modal.component.scss'],
})
export class ProductDetailModalComponent implements OnInit, OnDestroy {
  @Input()  product!: Product;
  @Output() close = new EventEmitter<void>();

  quantity = 1;

  constructor(
    private cartService: CartService,
    private toastService: ToastService,
  ) {}

  ngOnInit(): void    { document.body.style.overflow = 'hidden'; }
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
}