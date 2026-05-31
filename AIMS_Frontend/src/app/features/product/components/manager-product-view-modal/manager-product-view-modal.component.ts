import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product, ProductSummary, BookProduct, CdProduct, DvdProduct, NewspaperProduct } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';

@Component({
  selector: 'app-manager-product-view-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './manager-product-view-modal.component.html',
  styleUrl: './manager-product-view-modal.component.scss'
})
export class ManagerProductViewModalComponent implements OnInit, OnDestroy {
  /**
   * Pass either a full Product or a ProductSummary.
   * If only summary is available some fields will be missing – that's fine.
   */
  @Input() product!: Product;
  @Output() close = new EventEmitter<void>();

  readonly ProductType = ProductType;

  ngOnInit(): void {
    document.body.style.overflow = 'hidden';
  }

  ngOnDestroy(): void {
    document.body.style.overflow = '';
  }

  onClose(): void {
    this.close.emit();
  }

  formatPrice(price: number): string {
    return price.toLocaleString('vi-VN') + ' VND';
  }

  // ── Type guards / helpers ─────────────────────────────────────────────────

  get productType(): ProductType {
    return (this.product as any).productType as ProductType;
  }

  get typeLabel(): string {
    switch (this.productType) {
      case ProductType.BOOK:      return 'BOOK';
      case ProductType.CD:        return 'CD';
      case ProductType.DVD:       return 'DVD';
      case ProductType.NEWSPAPER: return 'NEWSPAPER';
      default:                    return String(this.productType);
    }
  }

  get typeClass(): string {
    switch (this.productType) {
      case ProductType.BOOK:      return 'badge-book';
      case ProductType.CD:        return 'badge-cd';
      case ProductType.DVD:       return 'badge-dvd';
      case ProductType.NEWSPAPER: return 'badge-newspaper';
      default:                    return '';
    }
  }

  get isBook():      boolean { return this.productType === ProductType.BOOK; }
  get isCd():        boolean { return this.productType === ProductType.CD; }
  get isDvd():       boolean { return this.productType === ProductType.DVD; }
  get isNewspaper(): boolean { return this.productType === ProductType.NEWSPAPER; }

  get bookDetails(): BookProduct['typeDetails'] | null {
    return this.isBook ? (this.product as BookProduct).typeDetails ?? null : null;
  }

  get cdDetails(): CdProduct['typeDetails'] | null {
    return this.isCd ? (this.product as CdProduct).typeDetails ?? null : null;
  }

  get dvdDetails(): DvdProduct['typeDetails'] | null {
    return this.isDvd ? (this.product as DvdProduct).typeDetails ?? null : null;
  }

  get npDetails(): NewspaperProduct['typeDetails'] | null {
    return this.isNewspaper ? (this.product as NewspaperProduct).typeDetails ?? null : null;
  }

  /** base fields shared by all types */
  get base(): any { return this.product as any; }
}
