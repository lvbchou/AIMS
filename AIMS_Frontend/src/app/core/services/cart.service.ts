// ==========================================================
// FILE: src/app/core/services/cart.service.ts
// Cart state service — real-time item count từ backend
// ==========================================================

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, timer } from 'rxjs';
import { switchMap, catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CartSummary {
  itemCount: number;   // tổng số sản phẩm (sum of quantities)
  totalPrice: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {

  private readonly apiBase = environment.apiBaseUrl;

  // BehaviorSubject giữ số lượng sản phẩm — emit mỗi khi có thay đổi
  private readonly _itemCount$ = new BehaviorSubject<number>(0);

  /** Observable cho header subscribe */
  readonly itemCount$: Observable<number> = this._itemCount$.asObservable();

  constructor(private readonly http: HttpClient) {
    // Poll backend mỗi 30 giây để sync số lượng
    this.startPolling();
  }

  // ── Backend API ──────────────────────────────────────────────

  /**
   * GET /api/cart/summary
   * Trả về { itemCount, totalPrice }
   */
  fetchCartSummary(): Observable<CartSummary> {
    return this.http
      .get<CartSummary>(`${this.apiBase}/cart/summary`)
      .pipe(
        catchError(() => {
          // Backend chưa ready → dùng mock
          return of({ itemCount: 2, totalPrice: 458000 });
        })
      );
  }

  /** Cập nhật count thủ công (gọi sau khi add/remove item) */
  refresh(): void {
    this.fetchCartSummary().subscribe(summary => {
      this._itemCount$.next(summary.itemCount);
    });
  }

  /** Gọi trực tiếp từ nơi khác để set count (tối ưu) */
  setCount(count: number): void {
    this._itemCount$.next(count);
  }

  // ── Polling ──────────────────────────────────────────────────

  private startPolling(): void {
    // Poll ngay khi init, sau đó mỗi 30s
    timer(0, 30_000)
      .pipe(
        switchMap(() => this.fetchCartSummary())
      )
      .subscribe(summary => {
        this._itemCount$.next(summary.itemCount);
      });
  }
}
