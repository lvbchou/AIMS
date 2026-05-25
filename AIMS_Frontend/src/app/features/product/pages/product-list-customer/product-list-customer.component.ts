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
  loading = false;
  errorMessage = '';           // SD 1.1.2 empty input / SD 1.1.4 no product found
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
      this.activeFilter = null;     // reset filter khi keyword thay đổi
      if (q) {
        this.mode = 'search';
        this.doSearch(q, undefined, undefined);
      } else {
        this.mode = 'all';
        this.products = MOCK_SUMMARIES;
        this.errorMessage = '';
      }
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  /**
   * SD step 2 (opt block): User tích checkbox rồi nhấn FILTER
   * hoặc nhấn CLEAR để bỏ filter.
   *
   * - range != null → filter trên kết quả search hiện tại (keyword + priceRange)
   * - range == null → quay về kết quả search ban đầu (chỉ keyword)
   */
  onFilterApplied(range: PriceRange | null): void {
    this.activeFilter = range;

    if (range) {
      this.mode = 'filter';
      const priceRange = `${range.min}-${range.max}`;
      // SD 2.1: gửi keyword + priceRange → backend filter trên search result
      this.doSearch(this.keyword || undefined, undefined, priceRange);
    } else {
      // User nhấn CLEAR hoặc bỏ tích checkbox rồi FILTER
      if (this.keyword) {
        this.mode = 'search';
        this.doSearch(this.keyword, undefined, undefined);
      } else {
        this.mode = 'all';
        this.products = MOCK_SUMMARIES;
        this.errorMessage = '';
      }
    }
  }

  /**
   * Gọi API search/filter kết hợp — 1 endpoint duy nhất.
   * keyword + category        → SD step 1: searchProduct
   * keyword + category + priceRange → SD step 2: filterByPriceRange(on search result)
   */
  private doSearch(keyword?: string, category?: string, priceRange?: string): void {
    this.loading = true;
    this.errorMessage = '';

    // Optimistic UI: client-side filter ngay lập tức
    this.applyClientSideFilter(keyword, priceRange);

    this.api.search(keyword, category, priceRange).subscribe({
      next: (results) => {
        this.loading = false;
        if (!results || results.length === 0) {
          // SD step 1.1.4: displayNoProductFoundNotification
          this.products = [];
          this.errorMessage = 'No product found.';
        } else {
          this.products = results;
          this.errorMessage = '';
        }
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 400) {
          // SD step 1.1.2: displayEmptyInputNotification
          this.errorMessage = err.error?.error ?? 'Please enter a product title or category.';
          this.products = [];
        } else {
          this.errorMessage = 'Something went wrong. Please try again.';
        }
      }
    });
  }

  /** Optimistic client-side — dùng MOCK khi chờ API trả về */
  private applyClientSideFilter(keyword?: string, priceRange?: string): void {
    let result = [...MOCK_SUMMARIES];
    if (keyword) {
      const k = keyword.toLowerCase();
      result = result.filter(p =>
        p.title.toLowerCase().includes(k) ||
        String(p.productType).toLowerCase().includes(k));
    }
    if (priceRange) {
      const [min, max] = priceRange.split('-').map(Number);
      result = result.filter(p => p.sellingPrice >= min && p.sellingPrice <= max);
    }
    this.products = result;
  }

  /** Filter panel chỉ hiện khi đang ở mode search hoặc filter */
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