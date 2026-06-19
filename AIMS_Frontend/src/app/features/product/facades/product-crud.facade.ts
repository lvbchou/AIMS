import { Injectable } from '@angular/core';
import { Observable, EMPTY } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

import { Product, ProductSummary } from '../models/product.model';
import { ProductService } from '../services/product.service';
import { ToastService } from '../../../core/services/toast/toast.service';
import { DialogService } from '../../../core/services/dialog/dialog.service';

/**
 * ProductCrudFacade — SRP: orchestrate CRUD operations with dialogs and notifications.
 *
 * Changes (SOLID refactoring):
 * - Return Observables instead of subscribing internally (no memory leaks)
 * - Component manages subscription lifecycle
 * - Toast notifications handled here via tap/catchError
 * - Delete operations still open dialogs internally (integrated confirmation UX)
 *
 * DIP: Component depends on this facade, not ProductService directly.
 */
@Injectable({ providedIn: 'root' })
export class ProductCrudFacade {

  constructor(
    private productService: ProductService,
    private toastService: ToastService,
    private dialogService: DialogService,
  ) {}

  // ── Read ───────────────────────────────────────────────────────────────────

  getById(productId: number): Observable<Product> {
    return this.productService.getById(productId);
  }

  // ── Create ─────────────────────────────────────────────────────────────

  create(product: Product): Observable<Product> {
    return this.productService.add(product).pipe(
      tap((res: any) => {
        this.toastService.show(
          res?.message ?? 'Product created successfully'
        );
      }),
      catchError(err => {
        console.error('[ProductCrudFacade] create error:', err);
        this.toastService.show(
          err?.error?.message ??
          'Failed to create product.'
        );
        return EMPTY;
      }),
    );
  }

  // ── Update ─────────────────────────────────────────────────────────────

  update(product: Product): Observable<void> {
    return this.productService.update(product).pipe(
      tap((res: any) => {
        this.toastService.show(
          res?.message ?? 'Product updated successfully'
        );
      }),
      catchError(err => {
        console.error('[ProductCrudFacade] update error:', err);
        this.toastService.show(
          err?.error?.message ??
          'Failed to update product.'
        );
        return EMPTY;
      }),
    );
  }

  // ── Delete single ──────────────────────────────────────────────────────

  confirmDelete(product: ProductSummary, onConfirm: () => void): void {
    this.dialogService.open({
      title: 'Confirm Delete',
      message: `Are you sure you want to delete "${product.title}"?`,
      onConfirm: () => {
        this.productService.delete(product.productId).pipe(
          tap((res: any) => {
            this.toastService.show(
              res?.message ?? 'Product deleted successfully'
            );
            onConfirm();
          }),
          catchError(err => {
            console.error('[ProductCrudFacade] delete error:', err);
            this.toastService.show(
              err?.error?.message ??
              'Failed to delete product. Please try again later.'
            );
            return EMPTY;
          }),
        ).subscribe();
      },
    });
  }

  // ── Delete many ────────────────────────────────────────────────────────

  confirmDeleteMany(
    ids: Set<number>,
    onConfirm: () => void,
    onCleanup: () => void,
  ): void {
    const count = ids.size;
    this.dialogService.open({
      title: 'Delete Selected',
      message: `Delete ${count} selected product${count > 1 ? 's' : ''}?`,
      onConfirm: () => {
        this.productService.deleteMany(Array.from(ids)).pipe(
          tap((res: any) => {
            this.toastService.show(
              res?.message ?? `Deleted ${count} product${count > 1 ? 's' : ''}`
            );
            onCleanup();
            onConfirm();
          }),
          catchError(err => {
            console.error('[ProductCrudFacade] deleteMany error:', err);
            onCleanup();
            this.toastService.show(
              err?.error?.message ??
              'Failed to delete products. Please try again later.'
            );
            return EMPTY;
          }),
        ).subscribe();
      },
    });
  }
}
