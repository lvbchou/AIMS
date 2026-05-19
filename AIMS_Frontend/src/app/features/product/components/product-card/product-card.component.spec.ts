import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Product {
  id: number;
  title: string;
  category: string;
  price: number;
  imageUrl: string;
}

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.scss']
})
export class ProductCardComponent {
  @Input() product!: Product;
  @Input() selected = false;
  @Input() selectionMode = false;

  @Output() toggle = new EventEmitter<void>();
  @Output() update = new EventEmitter<Product>();
  @Output() delete = new EventEmitter<Product>();

  onToggle() { this.toggle.emit(); }
  onUpdate() { this.update.emit(this.product); }
  onDelete() { this.delete.emit(this.product); }
}