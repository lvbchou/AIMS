import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Product } from '../../models/product.model';
import { ProductDetailPanelComponent } from '../product-detail-panel/product-detail-panel.component';

/**
 * ManagerProductViewModalComponent — manager shell.
 *
 * Chỉ chứa:
 *   - Modal lifecycle (scroll lock)
 *   - Price slot: selling + original
 *
 * Không inject CartService, không biết về ProductType.
 * Tất cả display delegate sang ProductDetailPanelComponent.
 */
@Component({
  selector: 'app-manager-product-view-modal',
  standalone: true,
  imports: [CommonModule, ProductDetailPanelComponent],
  templateUrl: './manager-product-view-modal.component.html',
  styleUrl: './manager-product-view-modal.component.scss',
})
export class ManagerProductViewModalComponent implements OnInit, OnDestroy {
  @Input()  product!: Product;
  @Output() close = new EventEmitter<void>();

  ngOnInit(): void    { document.body.style.overflow = 'hidden'; }
  ngOnDestroy(): void { document.body.style.overflow = ''; }

  onClose(): void { this.close.emit(); }

  fmt(n: number): string { return n.toLocaleString('vi-VN') + ' VND'; }
}