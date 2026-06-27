import { Component, OnInit, OnDestroy} from '@angular/core';
import { CommonModule, AsyncPipe } from '@angular/common';
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { Product, ProductSummary } from '../../models/product.model';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { ProductFormComponent } from '../../components/product-form/product-form.component';
import { ConfirmDialogComponent } from '../../../../shared/components/dialog/confirm-dialog/confirm-dialog.component';
import { ToastComponent } from '../../../../shared/components/toast/toast/toast.component';
import { ManagerProductViewModalComponent } from '../../components/manager-product-view-modal/manager-product-view-modal.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';

import { ProductListFacade } from '../../facades/product-list.facade';
import { ProductSelectionService } from '../../facades/product-selection.service';
import { ProductCrudFacade } from '../../facades/product-crud.facade';

import { ToastService } from '../../../../core/services/toast/toast.service';
/**
 * ProductManagementComponent — AFTER Phase 3 refactoring.
 *
 * Responsibilities (SRP):
 *   - Điều phối UI state: showProductForm, selectedProduct, viewDetailProduct
 *   - Delegate list/pagination  → ProductListFacade
 *   - Delegate selection state  → ProductSelectionService
 *   - Delegate CRUD + dialogs   → ProductCrudFacade
 *
 * Không còn: ChangeDetectorRef, ToastService, DialogService,
 * ProductService trực tiếp, hay bất kỳ business logic nào.
 */
@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [
    CommonModule,
    AsyncPipe,
    ProductListComponent,
    ProductFormComponent,
    ConfirmDialogComponent,
    ToastComponent,
    ManagerProductViewModalComponent,
    PaginationComponent,
  ],
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.scss',
})
export class ProductManagementComponent implements OnInit, OnDestroy {
  private readonly _showProductForm$ = new BehaviorSubject(false);
  private readonly _selectedProduct$ = new BehaviorSubject<Product | null>(null);
  private readonly _viewDetailProduct$ = new BehaviorSubject<Product | null>(null);

  showProductForm$ = this._showProductForm$.asObservable();
  selectedProduct$ = this._selectedProduct$.asObservable();
  viewDetailProduct$ = this._viewDetailProduct$.asObservable();

  // Fallback cho async pipe trả về null trước khi Observable emit lần đầu
  readonly emptySet = new Set<number>();

  private readonly destroy$ = new Subject<void>();

  constructor(
    readonly listFacade: ProductListFacade,
    readonly selection: ProductSelectionService,
    readonly crud: ProductCrudFacade,
    private toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.listFacade.initFromRoute();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Selection ──────────────────────────────────────────────────────────────

  onToggle(productId: number): void {
    this.selection.toggle(productId);
  }

  enterSelectionMode(): void {
    this.selection.enterSelectionMode();
  }

  cancelSelection(): void {
    this.selection.cancelSelection();
  }

  // ── Form open/close ────────────────────────────────────────────────────────

  onAdd(): void {
    this._selectedProduct$.next(null);
    this._showProductForm$.next(true);
  }

  onUpdate(productId: number): void {
    this.crud.getById(productId)
      .pipe(takeUntil(this.destroy$))
      .subscribe(product => {
        this._selectedProduct$.next(product);
        this._showProductForm$.next(true);
      });
  }

  onCreateCancelled(): void {
    this._showProductForm$.next(false);
    this._selectedProduct$.next(null);
  }

  // ── CRUD submit ────────────────────────────────────────────────────────────

  onFormSubmitted(product: Product): void {
    const isUpdate = this._selectedProduct$.value !== null;
    const onSuccess = () => {
      this._showProductForm$.next(false);
      this._selectedProduct$.next(null);
      this.listFacade.reload();
    };

    if (isUpdate) {
      this.crud.update(product)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => onSuccess());
    } else {
      this.crud.create(product)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => onSuccess());
    }
  }

  onDelete(product: ProductSummary): void {
    this.crud.confirmDelete(product, () => this.listFacade.reload());
  }

  deleteSelected(): void {
    this.crud.confirmDeleteMany(
      this.selection.selectedIds,
      () => this.listFacade.reload(),
      () => this.selection.clearAfterDelete(),
    );
  }

  // ── Detail view ────────────────────────────────────────────────────────────

  onViewDetail(productId: number): void {
    this.crud.getById(productId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: product => {
          this._viewDetailProduct$.next(product);
        },
        error: () => this.toastService.show('Failed to load product details. Please try again'),
      });
  }

  closeViewDetail(): void {
    this._viewDetailProduct$.next(null);
  }

  // ── Pagination ─────────────────────────────────────────────────────────────

  goToPage(page: number): void {
    this.listFacade.goToPage(page);
  }
}