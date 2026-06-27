import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject, takeUntil } from 'rxjs';
import { map } from 'rxjs/operators';
import { Router, ActivatedRoute } from '@angular/router';

import { ProductSummary } from '../models/product.model';
import { ProductService } from '../services/product.service';
import { PriceRange } from '../components/product-filter/product-filter.component';

export type CatalogMode = 'all' | 'search' | 'filter';

export interface CatalogState {
  products:      ProductSummary[];
  isLoading:     boolean;
  mode:          CatalogMode;
  keyword:       string;
  activeFilter:  PriceRange | null;
  currentPage:   number;
  totalPages:    number;
  totalElements: number;
}

const INITIAL_STATE: CatalogState = {
  products:      [],
  isLoading:     false,
  mode:          'all',
  keyword:       '',
  activeFilter:  null,
  currentPage:   0,
  totalPages:    0,
  totalElements: 0,
};

/**
 * ProductCatalogFacade — SOLID refactoring: single source of truth.
 *
 * Changes:
 * - Only one BehaviorSubject: _state$
 * - All public observables derived from state$ (DRY principle)
 * - Single state mutations via patch() method
 * - High cohesion, low coupling
 *
 * Public API unchanged for backward compatibility.
 */
@Injectable()
export class ProductCatalogFacade {

  private readonly PAGE_SIZE = 10;
  private readonly _state$ = new BehaviorSubject<CatalogState>(INITIAL_STATE);

  readonly state$: Observable<CatalogState> = this._state$.asObservable();

  // Derived selectors — all computed from state$ (no duplication)
  readonly products$:      Observable<ProductSummary[]> = this.state$.pipe(map(s => s.products));
  readonly isLoading$:     Observable<boolean>           = this.state$.pipe(map(s => s.isLoading));
  readonly totalPages$:    Observable<number>            = this.state$.pipe(map(s => s.totalPages));
  readonly totalElements$: Observable<number>            = this.state$.pipe(map(s => s.totalElements));
  readonly currentPage$:   Observable<number>            = this.state$.pipe(map(s => s.currentPage));
  readonly mode$:          Observable<CatalogMode>       = this.state$.pipe(map(s => s.mode));
  readonly keyword$:       Observable<string>            = this.state$.pipe(map(s => s.keyword));

  constructor(
    private productService: ProductService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  /**
   * Initialize facade — read query params and load appropriate mode.
   * Call in ngOnInit, pass destroy$ for auto-unsubscribe.
   */
  init(destroy$: Observable<void>): void {
    this.route.queryParams.pipe(takeUntil(destroy$)).subscribe(params => {
      const page    = (params['page'] ?? 1) - 1;
      const keyword = (params['q'] ?? '') as string;

      this.patch({ currentPage: page, keyword });

      const { activeFilter } = this._state$.getValue();

      if (activeFilter) {
        this.patch({ mode: 'filter' });
        this.fetchFilter(activeFilter, keyword, page);
      } else if (keyword) {
        this.patch({ mode: 'search' });
        this.fetchSearch(keyword, page);
      } else {
        this.patch({ mode: 'all' });
        this.fetchAll(page);
      }
    });
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  goToPage(page: number): void {
    const { totalPages } = this._state$.getValue();
    if (page < 0 || page >= totalPages) return;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: page + 1 },
      queryParamsHandling: 'merge',
    });
  }

  applyFilter(range: PriceRange | null): void {
    this.patch({ activeFilter: range, currentPage: 0 });

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: 1 },
      queryParamsHandling: 'merge',
    });

    const { keyword } = this._state$.getValue();

    if (range) {
      this.patch({ mode: 'filter' });
      this.fetchFilter(range, keyword, 0);
    } else if (keyword) {
      this.patch({ mode: 'search' });
      this.fetchSearch(keyword, 0);
    } else {
      this.patch({ mode: 'all' });
      this.fetchAll(0);
    }
  }

  loadDetail(id: number) {
    return this.productService.getById(id);
  }

  // ── Derived getters (for template use) ──────────────────────────────────────

  get snapshot(): CatalogState { return this._state$.getValue(); }

  get showFilter(): boolean {
    const { mode } = this._state$.getValue();
    return mode === 'search' || mode === 'filter';
  }

  get pageTitle(): string {
    const { mode, keyword } = this._state$.getValue();
    switch (mode) {
      case 'search': return `Search results for "${keyword}"`;
      case 'filter': return 'Filter results';
      default:       return 'New Arrivals';
    }
  }

  // ── Private fetch methods ──────────────────────────────────────────────────

  private fetchAll(page: number): void {
    this.patch({ isLoading: true });
    this.productService.getAll(page, this.PAGE_SIZE).subscribe({
      next:  (data) => this.applyPagedResult(data),
      error: (err)  => { console.error(err); this.patch({ isLoading: false }); },
    });
  }

  private fetchSearch(keyword: string, page: number): void {
  this.patch({ isLoading: true });
  // Truyền cùng chuỗi vào keyword + category → backend match title HOẶC category
  this.productService.search(keyword, keyword, page, this.PAGE_SIZE).subscribe({
    next:  (data) => this.applyPagedResult(data),
    error: ()     => this.patch({ isLoading: false }),
  });
}

  private fetchFilter(range: PriceRange, keyword: string, page: number): void {
  this.patch({ isLoading: true });
  this.productService.filterByPrice(
    range.min, range.max,
    keyword || undefined,
    keyword || undefined,   // ← category cũng nhận keyword (trước đây là undefined)
    page,
    this.PAGE_SIZE,
  ).subscribe({
    next:  (data) => this.applyPagedResult(data),
    error: ()     => this.patch({ isLoading: false }),
  });
}

  private applyPagedResult(data: { content: ProductSummary[]; totalPages: number; totalElements: number }): void {
    this.patch({
      products:      data.content,
      totalPages:    data.totalPages,
      totalElements: data.totalElements,
      isLoading:     false,
    });
  }

  private patch(partial: Partial<CatalogState>): void {
    this._state$.next({ ...this._state$.getValue(), ...partial });
  }
}
