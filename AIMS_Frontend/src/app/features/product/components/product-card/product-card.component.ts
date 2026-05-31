import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ProductSummary } from '../../models/product.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss'
})
export class ProductCardComponent {

  @Input() product!: ProductSummary;
  @Input() selected: boolean = false;
  @Input() selectionMode: boolean = false;
  @Input() isMaxSelected = false;
  @Input() mode: 'manager' | 'customer' = 'manager';

  @Output() viewDetail   = new EventEmitter<number>();
  @Output() addToCart    = new EventEmitter<number>();
  @Output() update = new EventEmitter<number>();
  @Output() delete = new EventEmitter<ProductSummary>();
  @Output() toggleSelect = new EventEmitter<number>();

  onToggle() {
    this.toggleSelect.emit(this.product.productId);
  }

  onUpdate() {
    this.update.emit(this.product.productId);
  }

  onDelete() {
    this.delete.emit(this.product);
  }

  onImageClick()  { this.viewDetail.emit(this.product.productId); }
  onAddToCart()   { this.addToCart.emit(this.product.productId); }

  get isCustomer(): boolean { return this.mode === 'customer'; }
  get isManager():  boolean { return this.mode === 'manager'; }
}
