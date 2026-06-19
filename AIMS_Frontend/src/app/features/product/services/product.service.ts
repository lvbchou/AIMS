import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product, ProductSummary } from '../models/product.model';
import { mapToProduct, mapToPayload } from '../models/product.mapper';
import { environment } from '../../../../environments/environment';
import { Page } from '../../../shared/models/page.model';

@Injectable({ providedIn: 'root' })
export class ProductService {

  private readonly BASE_URL = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  // ── Get all (summary) ─────────────────────────────────────────────
  getAll(page = 0, size = 10): Observable<Page<ProductSummary>> {
    return this.http.get<Page<ProductSummary>>(
      `${this.BASE_URL}?page=${page}&size=${size}`
    );
  }

  getByIds(ids: number[]): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(
      `${this.BASE_URL}/batch`,
      {
        params: {
          ids: ids.join(',')
        }
      }
    );
  }

  // ── Get by id (full detail) ───────────────────────────────────────
  getById(id: number): Observable<Product> {
    return this.http.get<any>(`${this.BASE_URL}/${id}`).pipe(
      map(mapToProduct)  // ← flat → nested
    );
  }

  // ── Add ───────────────────────────────────────────────────────────
  add(product: Product): Observable<Product> {
    return this.http.post<any>(this.BASE_URL, mapToPayload(product));
  }

  // ── Update ────────────────────────────────────────────────────────
  update(product: Product): Observable<void> {
    return this.http.put<void>(
      `${this.BASE_URL}/${product.productId}`,
      mapToPayload(product)  // ← nested → flat
    );
  }

  // ── Delete single ─────────────────────────────────────────────────
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/${id}`);
  }

  // ── Delete multiple ───────────────────────────────────────────────
  deleteMany(ids: number[]): Observable<void> {
    return this.http.delete<void>(this.BASE_URL, { body: ids });
  }

  // ── Search ────────────────────────────────────────────────────────
  search(keyword: string, category?: string, page = 0, size = 10): Observable<Page<ProductSummary>> {
    let params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page)
      .set('size', size);
    if (category) params = params.set('category', category);
    return this.http.get<Page<ProductSummary>>(`${this.BASE_URL}/search`, { params });
  }

  // ── Filter by price ───────────────────────────────────────────────
  filterByPrice(min: number, max: number, keyword?: string, category?: string, page = 0, size = 10): Observable<Page<ProductSummary>> {
    let params = new HttpParams()
      .set('priceRange', `${min}-${max}`)
      .set('page', page)
      .set('size', size);
    if (keyword)  params = params.set('keyword',  keyword);
    if (category) params = params.set('category', category);
    return this.http.get<Page<ProductSummary>>(`${this.BASE_URL}/search`, { params });
  }
}
