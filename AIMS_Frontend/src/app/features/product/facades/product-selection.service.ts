import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map } from 'rxjs';
import { ToastService } from '../../../core/services/toast/toast.service';

const MAX_SELECTION = 10;

/**
 * ProductSelectionService — SRP: chỉ quản lý trạng thái chọn sản phẩm.
 *
 * Dùng BehaviorSubject để component có thể subscribe và tự động re-render
 * khi selection thay đổi — không cần gọi cdr.detectChanges() thủ công.
 */
@Injectable({ providedIn: 'root' })
export class ProductSelectionService {

  private readonly _selectedIds$ = new BehaviorSubject<Set<number>>(new Set());
  private readonly _selectionMode$ = new BehaviorSubject<boolean>(false);

  readonly selectedIds$: Observable<Set<number>>  = this._selectedIds$.asObservable();
  readonly selectionMode$: Observable<boolean>    = this._selectionMode$.asObservable();
  readonly selectedCount$: Observable<number>     = this._selectedIds$.pipe(map(s => s.size));
  readonly isMaxSelected$: Observable<boolean>    = this._selectedIds$.pipe(map(s => s.size >= MAX_SELECTION));

  // Snapshot getters — dùng trong template hoặc khi cần giá trị tức thời
  get selectedIds(): Set<number>  { return this._selectedIds$.getValue(); }
  get selectionMode(): boolean    { return this._selectionMode$.getValue(); }
  get selectedCount(): number     { return this._selectedIds$.getValue().size; }

  constructor(private toastService: ToastService) {}

  enterSelectionMode(): void {
    this._selectionMode$.next(true);
  }

  cancelSelection(): void {
    this._selectedIds$.next(new Set());
    this._selectionMode$.next(false);
  }

  toggle(productId: number): void {
    const current = new Set(this._selectedIds$.getValue());

    if (current.has(productId)) {
      current.delete(productId);
    } else {
      if (current.size >= MAX_SELECTION) {
        this.toastService.show(`Reach maximum selection of ${MAX_SELECTION} products at once`);
        return;
      }
      current.add(productId);
    }

    this._selectedIds$.next(current);
    this._selectionMode$.next(current.size > 0);
  }

  clearAfterDelete(): void {
    this._selectedIds$.next(new Set());
    this._selectionMode$.next(false);
  }
}