import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, AsyncPipe } from '@angular/common';
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { ProductDetailModalComponent } from '../../components/product-detail-modal/product-detail-modal.component';
import { ProductFilterComponent, PriceRange } from '../../components/product-filter/product-filter.component';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { ToastComponent } from '../../../../shared/components/toast/toast/toast.component';

import { Product } from '../../models/product.model';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { ProductCatalogFacade } from '../../facades/product-catalog.facade';
import { CartService } from '../../../cart/services/cart.service';

@Component({
  selector: 'app-product-list-customer',
  standalone: true,
  imports: [
    CommonModule,
    AsyncPipe,
    ProductListComponent,
    ProductDetailModalComponent,
    ProductFilterComponent,
    PaginationComponent,
    ToastComponent,
  ],
  templateUrl: './product-list-customer.component.html',
  styleUrl: './product-list-customer.component.scss',
  providers: [ProductCatalogFacade], // scoped — không share state với trang khác
})
export class ProductListCustomerComponent implements OnInit, OnDestroy {

  private readonly _viewProductDetail$ = new BehaviorSubject<Product | null>(null);
  readonly viewProductDetail$ = this._viewProductDetail$.asObservable();

  private readonly destroy$ = new Subject<void>();

  constructor(
    readonly catalog: ProductCatalogFacade,
    private toastService: ToastService,
    private cartService: CartService,
  ) {}

  ngOnInit(): void {
    this.catalog.init(this.destroy$.asObservable());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Filter ─────────────────────────────────────────────────────────────────

  onFilterApplied(range: PriceRange | null): void {
    this.catalog.applyFilter(range);
  }

  // ── Pagination ─────────────────────────────────────────────────────────────

  goToPage(page: number): void {
    this.catalog.goToPage(page);
  }

  // ── Detail modal ───────────────────────────────────────────────────────────

  onViewDetail(id: number): void {
    this.catalog.loadDetail(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next:  (product) => { 
          console.log('Loaded product detail:', product);
          this._viewProductDetail$.next(product);
        },
        error: () => this.toastService.show('Failed to load product details. Please try again'),
      });
  }

  closeModal(): void {
    this._viewProductDetail$.next(null);
  }

  onAddToCart(id: number): void {
    this.cartService.addToCart(id, 1);
    this.toastService.show('Added to cart');
  }
}