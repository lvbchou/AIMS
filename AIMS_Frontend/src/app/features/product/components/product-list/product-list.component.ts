import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductCardComponent } from '../product-card/product-card.component';
import { ProductSummary } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';
import { ProductCardSkeletonComponent } from '../../skeleton/product-card-skeleton/product-card-skeleton.component';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, ProductCardSkeletonComponent],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent {
  @Input() products: ProductSummary[] = [];
  @Input() mode: 'manager' | 'customer' = 'manager';
  @Input() selectedIds: Set<number> = new Set();
  @Input() selectionMode = false;
  @Input() isMaxSelected = false;
  @Input() isLoading = false;
  @Input() skeletonCount = 10;

  @Output() toggleSelect = new EventEmitter<number>();
  @Output() onUpdate = new EventEmitter<number>();
  @Output() onDelete = new EventEmitter<ProductSummary>();
  @Output() onViewDetail = new EventEmitter<number>();
  @Output() onAddToCart = new EventEmitter<number>();

  get skeletonItems(): number[] {
    return Array(this.skeletonCount).fill(0);
  }

  isSelected(productId: number): boolean {
    return this.selectedIds.has(productId);
  }

  onToggle(productId: number) {
    this.toggleSelect.emit(productId);
  }
}