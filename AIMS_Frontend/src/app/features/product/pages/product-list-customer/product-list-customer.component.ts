import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ProductCardComponent } from '../../components/product-card/product-card.component';
import { ProductDetailModalComponent } from '../../components/product-detail-modal/product-detail-modal.component';
import { ProductFilterComponent, PriceRange } from '../../components/product-filter/product-filter.component';
import { ProductService } from '../../services/product.service';
import { ProductSummary } from '../../models/product.model';
import { MOCK_SUMMARIES } from '../../data/mock-products.data';

type Mode = 'all' | 'search' | 'filter';

@Component({
  selector: 'app-product-list-customer',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, ProductDetailModalComponent, ProductFilterComponent],
  templateUrl: './product-list-customer.component.html',
  styleUrl: './product-list-customer.component.scss'
})
export class ProductListCustomerComponent implements OnInit, OnDestroy {
  products: ProductSummary[] = [];
  private searchResults: ProductSummary[] = [];  // SD: productList — lưu để filter client-side
  loading = false;
  errorMessage = '';
  mode: Mode = 'all';
  keyword = '';
  activeFilter: PriceRange | null = null;
  selectedProductId: number | null = null;
  private destroy$ = new Subject<void>();

  constructor(private api: ProductService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.products = MOCK_SUMMARIES;
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const q = (params['q'] ?? '') as string;
      this.keyword = q;
      this.activeFilter = null;
      if (q) {
        this.mode = 'search';
        this.doSearch(q);
      } else {
        this.mode = 'all';
        this.products = MOCK_SUMMARIES;
        this.searchResults = [];
        this.errorMessage = '';
      }
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  /**
   * SD step 2 (opt block): User chọn price range → filter client-side trên searchResults
   * Không gọi lại API — đúng với SD 2.1.1: filterByPriceRange(productList, priceRange)
   */
  onFilterApplied(range: PriceRange | null): void {
    this.activeFilter = range;

    if (range) {
      // SD 2.1.1: filterByPriceRange(productList, priceRange)
      this.mode = 'filter';
      this.products = this.searchResults.filter(
        p => p.sellingPrice >= range.min && p.sellingPrice <= range.max
      );
      // SD 2.1.2: displayFilteredProductList
      this.errorMessage = this.products.length === 0 ? 'No product found.' : '';
    } else {
      // User nhấn CLEAR → quay về search result ban đầu
      if (this.keyword) {
        this.mode = 'search';
        this.products = this.searchResults;
        this.errorMessage = '';
      } else {
        this.mode = 'all';
        this.products = MOCK_SUMMARIES;
        this.errorMessage = '';
      }
    }
  }

  /**
   * SD step 1.1: searchProduct(keyword, category)
   * Chỉ gọi API search — filter giá xử lý client-side sau
   */
  private doSearch(keyword?: string, category?: string): void {
    this.loading = true;
    this.errorMessage = '';

    // Optimistic UI: hiện mock ngay trong lúc chờ API
    this.applyClientSideSearch(keyword);

    this.api.search(keyword, category).subscribe({
      next: (results) => {
        this.loading = false;
        if (!results || results.length === 0) {
          // SD step 1.1.4: displayNoProductFoundNotification
          this.products = [];
          this.searchResults = [];
          this.errorMessage = 'No product found.';
        } else {
          // SD step 1.1.5/1.1.6: showSearchingResult
          this.products = results;
          this.searchResults = results;  // lưu lại để filter sau
          this.errorMessage = '';
        }
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 400) {
          // SD step 1.1.2: displayEmptyInputNotification
          this.errorMessage = err.error?.error ?? 'Please enter a product title or category.';
          this.products = [];
          this.searchResults = [];
        } else {
          // API lỗi (BE chưa chạy, network lỗi...) → fallback về mock để filter vẫn hoạt động
          this.applyClientSideSearch(keyword);
          this.searchResults = [...this.products];
          this.errorMessage = '';
        }
      }
    });
  }

  /** Optimistic UI + fallback khi API lỗi — lọc MOCK theo keyword */
  private applyClientSideSearch(keyword?: string): void {
    if (!keyword) {
      this.products = [...MOCK_SUMMARIES];
      return;
    }
    const k = keyword.toLowerCase();
    this.products = MOCK_SUMMARIES.filter(p =>
      p.title.toLowerCase().includes(k) ||
      String(p.productType).toLowerCase().includes(k)
    );
  }

  get showFilter(): boolean { return this.mode === 'search' || this.mode === 'filter'; }

  get pageTitle(): string {
    if (this.mode === 'search') return 'Search result ...';
    if (this.mode === 'filter') return 'Filter result ...';
    return 'New Arrivals';
  }

  onViewDetail(id: number): void  { this.selectedProductId = id; }
  onAddToCart(id: number): void   { console.log('Add to cart:', id); }
  closeModal(): void               { this.selectedProductId = null; }
}