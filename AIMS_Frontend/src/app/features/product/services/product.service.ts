import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, ProductSummary } from '../models/product.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly BASE_URL = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  // ── Get all ───────────────────────────────────────────────────────
  getAll(): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(this.BASE_URL);
  }

  // ── Get by id ─────────────────────────────────────────────────────
  getById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.BASE_URL}/${id}`);
  }

  // ── Add ───────────────────────────────────────────────────────────
  add(product: Omit<Product, 'id'>): Observable<Product> {
    return this.http.post<Product>(this.BASE_URL, product);
  }

  // ── Update ────────────────────────────────────────────────────────
  update(id: number, product: Partial<Product>): Observable<Product> {
    return this.http.put<Product>(`${this.BASE_URL}/${id}`, product);
  }

  // ── Delete single ─────────────────────────────────────────────────
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/${id}`);
  }

  // ── Delete multiple ───────────────────────────────────────────────
  deleteMany(ids: number[]): Observable<void> {
    return this.http.delete<void>(this.BASE_URL, { body: ids });
  }

  // ── Search (SD step 1.1.3) ────────────────────────────────────────
  search(keyword?: string, category?: string, p?: any): Observable<ProductSummary[]> {
    let params = new HttpParams();
    if (keyword)  params = params.set('keyword', keyword);
    if (category) params = params.set('category', category);
    return this.http.get<ProductSummary[]>(`${this.BASE_URL}/search`, { params });
  }

  // ── Filter by price range (SD step 2.1.1) ────────────────────────
  // Độc lập — không cần search trước, query thẳng DB theo giá
  // priceRange format: "min-max" e.g. "100000-200000"
  filterByPrice(min: number, max: number): Observable<ProductSummary[]> {
    const params = new HttpParams().set('priceRange', `${min}-${max}`);
    return this.http.get<ProductSummary[]>(`${this.BASE_URL}/filter`, { params });
  }
}