import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

import { ProductDetailModalComponent } from '../../components/product-detail-modal/product-detail-modal.component';
import { ProductFilterComponent, PriceRange } from '../../components/product-filter/product-filter.component';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { ToastComponent } from '../../../../shared/components/toast/toast/toast.component';

import { ProductService } from '../../services/product.service';
import { ToastService } from '../../../../core/services/toast.service';
import { Product, ProductSummary } from '../../models/product.model';

type Mode = 'all' | 'search' | 'filter';

@Component({
  selector: 'app-product-list-customer',
  standalone: true,
  imports: [
    CommonModule,
    ProductListComponent,
    ProductDetailModalComponent,
    ProductFilterComponent,
    PaginationComponent,
    ToastComponent,
  ],
  templateUrl: './product-list-customer.component.html',
  styleUrl: './product-list-customer.component.scss'
})
export class ProductListCustomerComponent implements OnInit, OnDestroy {

  products:      ProductSummary[] = [];
  isLoading      = false;
  mode: Mode     = 'all';
  keyword        = '';
  activeFilter:  PriceRange | null = null;
  viewProductDetail: Product | null = null;

  currentPage   = 0;
  pageSize      = 10;
  totalPages    = 0;
  totalElements = 0;

  private destroy$ = new Subject<void>();

  constructor(
    private api:          ProductService,
    private route:        ActivatedRoute,
    private router:       Router,
    private cdr:          ChangeDetectorRef,
    private toastService: ToastService,
  ) {}

  ngOnInit(): void {
    // Đọc page + query từ URL → giữ được trang khi reload
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.currentPage = (params['page'] ?? 1) - 1;
      const q = (params['q'] ?? '') as string;
      this.keyword = q;

      if (this.activeFilter) {
        // Đang filter → load lại với page mới
        this.mode = 'filter';
        this.apiFilter(this.activeFilter);
      } else if (q) {
        this.mode = 'search';
        this.apiSearch(q);
      } else {
        this.mode = 'all';
        this.loadProducts();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Pagination ─────────────────────────────────────────────────────────────
  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;

    // Cập nhật URL → queryParams subscribe tự trigger load đúng mode
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: page + 1 },
      queryParamsHandling: 'merge',
    });
  }

  // ── Filter ─────────────────────────────────────────────────────────────────
  onFilterApplied(range: PriceRange | null): void {
    this.activeFilter = range;
    this.currentPage  = 0;

    // Reset page về 0 trên URL
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: 1 },
      queryParamsHandling: 'merge',
    });

    if (range) {
      this.mode = 'filter';
      this.apiFilter(range);
    } else if (this.keyword) {
      this.mode = 'search';
      this.apiSearch(this.keyword);
    } else {
      this.mode = 'all';
      this.loadProducts();
    }
  }

  // ── Getters ────────────────────────────────────────────────────────────────
  get showFilter(): boolean {
    return this.mode === 'search' || this.mode === 'filter';
  }

  get pageTitle(): string {
    switch (this.mode) {
      case 'search': return `Search results for "${this.keyword}"`;
      case 'filter': return 'Filter results';
      default:       return 'New Arrivals';
    }
  }

  // ── Load all ───────────────────────────────────────────────────────────────
  private loadProducts(): void {
    this.isLoading = true;
    this.api.getAll(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.products      = data.content;
        this.totalPages    = data.totalPages;
        this.totalElements = data.totalElements;
        this.isLoading     = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ── Search ─────────────────────────────────────────────────────────────────
  private apiSearch(kw: string): void {
    this.isLoading = true;
    this.api.search(kw, undefined, this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.products      = data.content;
        this.totalPages    = data.totalPages;
        this.totalElements = data.totalElements;
        this.isLoading     = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ── Filter by price (áp dụng lên kết quả search hiện tại) ────────────────
  private apiFilter(range: PriceRange): void {
    this.isLoading = true;
    this.api.filterByPrice(
      range.min, range.max,
      this.keyword || undefined,  // truyền keyword nếu đang search
      undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (data) => {
        this.products = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ── Detail modal ───────────────────────────────────────────────────────────
  onViewDetail(id: number): void {
    this.api.getById(id).subscribe({
      next: (product) => {
        this.viewProductDetail = product;
        this.cdr.detectChanges();
      },
      error: () => this.toastService.show('Failed to load product details'),
    });
  }

  closeModal(): void {
    this.viewProductDetail = null;
  }

  // ── Cart ───────────────────────────────────────────────────────────────────
  onAddToCart(id: number): void {
    this.toastService.show('Added to cart');
  }
}
