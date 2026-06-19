import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map } from 'rxjs';
import { Router, ActivatedRoute } from '@angular/router';

import { Product, ProductSummary } from '../models/product.model';
import { ProductService } from '../services/product.service';

export interface ListState {
  products:      ProductSummary[];
  isLoading:     boolean;
  totalPages:    number;
  totalElements: number;
  currentPage:   number;
}

const INITIAL_STATE: ListState = {
  products:      [],
  isLoading:     false,
  totalPages:    0,
  totalElements: 0,
  currentPage:   0,
};

/**
 * ProductListFacade — SOLID refactoring: consolidated state.
 *
 * Changes:
 * - Single BehaviorSubject for state
 * - All observables derived from state$ (DRY)
 * - High cohesion: related state updates together
 * - Reused by HomeComponent and ProductManagementComponent
 */
@Injectable({ providedIn: 'root' })
export class ProductListFacade {

  private readonly PAGE_SIZE = 10;
  private readonly _state$ = new BehaviorSubject<ListState>(INITIAL_STATE);

  readonly state$: Observable<ListState> = this._state$.asObservable();

  // Derived selectors
  readonly products$:      Observable<ProductSummary[]> = this.state$.pipe(map(s => s.products));
  readonly isLoading$:     Observable<boolean>          = this.state$.pipe(map(s => s.isLoading));
  readonly totalPages$:    Observable<number>           = this.state$.pipe(map(s => s.totalPages));
  readonly totalElements$: Observable<number>           = this.state$.pipe(map(s => s.totalElements));
  readonly currentPage$:   Observable<number>           = this.state$.pipe(map(s => s.currentPage));

  get totalPages(): number  { return this._state$.getValue().totalPages; }
  get currentPage(): number { return this._state$.getValue().currentPage; }

  constructor(
    private productService: ProductService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  initFromRoute(): void {
    this.route.queryParams.subscribe(params => {
      const page = (params['page'] ?? 1) - 1;
      this.patch({ currentPage: page });
      this.loadPage(page);
    });
  }

  reload(): void {
    this.loadPage(this._state$.getValue().currentPage);
  }

  getById(id: number): Observable<Product> {
    return this.productService.getById(id);
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this._state$.getValue().totalPages) return;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: page + 1 },
      queryParamsHandling: 'merge',
    });
  }

  private loadPage(page: number): void {
    this.patch({ isLoading: true });
    this.productService.getAll(page, this.PAGE_SIZE).subscribe({
      next: (data) => {
        this.patch({
          products:      data.content,
          totalPages:    data.totalPages,
          totalElements: data.totalElements,
          isLoading:     false,
        });
      },
      error: (err) => {
        console.error('[ProductListFacade] loadPage error:', err);
        this.patch({ isLoading: false });
      },
    });
  }

  private patch(partial: Partial<ListState>): void {
    this._state$.next({ ...this._state$.getValue(), ...partial });
  }
}
