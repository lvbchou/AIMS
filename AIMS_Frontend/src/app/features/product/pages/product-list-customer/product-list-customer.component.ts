import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
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
      if (q) { this.mode = 'search'; this.clientSearch(q); this.apiSearch(q); }
      else   { this.mode = 'all';    this.products = MOCK_SUMMARIES;            }
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  onFilterApplied(range: PriceRange | null): void {
    this.activeFilter = range;
    if (range) { this.mode = 'filter'; this.clientFilter(range); this.apiFilter(range); }
    else if (this.keyword) { this.mode = 'search'; this.clientSearch(this.keyword); }
    else { this.mode = 'all'; this.products = MOCK_SUMMARIES; }
  }

  get showFilter(): boolean { return this.mode === 'search' || this.mode === 'filter'; }
  get pageTitle(): string {
    return this.mode === 'search' ? 'Search result ...' :
           this.mode === 'filter' ? 'Filter result ...' : 'New Arrivals';
  }

  private clientSearch(kw: string): void {
    const k = kw.toLowerCase();
    this.products = MOCK_SUMMARIES.filter(p =>
      p.title.toLowerCase().includes(k) || String(p.productType).toLowerCase().includes(k));
  }
  private clientFilter(r: PriceRange): void {
    this.products = MOCK_SUMMARIES.filter(p => {
      const ok = p.sellingPrice >= r.min && p.sellingPrice <= r.max;
      return ok && (!this.keyword || p.title.toLowerCase().includes(this.keyword.toLowerCase()));
    });
  }
  private apiSearch(kw: string): void {
    this.api.search(kw).subscribe({ next: r => { if (r?.length) this.products = r; }, error: () => {} });
  }
  private apiFilter(r: PriceRange): void {
    this.api.filterByPrice(r.min, r.max).subscribe({ next: res => { if (res?.length) this.products = res; }, error: () => {} });
  }

  onViewDetail(id: number): void  { this.selectedProductId = id; }
  onAddToCart(id: number): void   { console.log('Add to cart:', id); }
  closeModal(): void               { this.selectedProductId = null; }
}
