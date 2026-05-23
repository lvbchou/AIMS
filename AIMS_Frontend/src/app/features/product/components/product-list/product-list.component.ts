import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductCardComponent } from '../product-card/product-card.component';
import { ProductSummary } from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, ProductCardComponent],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.scss'
})
export class ProductListComponent {

  @Input() products: ProductSummary[] = [];
  @Input() isLoading = false;
  @Input() skeletonCount = 10;
  @Input() selectedIds: Set<number> = new Set();
  @Input() selectionMode = false;
  @Input() isMaxSelected = false;

  @Output() toggleSelect  = new EventEmitter<number>();
  @Output() onUpdate      = new EventEmitter<number>();
  @Output() onDelete      = new EventEmitter<number>();
  @Output() onViewDetail  = new EventEmitter<number>();

  get skeletonItems(): number[] {
    return Array.from({ length: this.skeletonCount }, (_, i) => i);
  }

  isSelected(productId: number): boolean {
    return this.selectedIds.has(productId);
  }

  onToggle(productId: number): void {
    this.toggleSelect.emit(productId);
  }
}