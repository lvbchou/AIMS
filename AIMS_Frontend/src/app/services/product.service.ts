import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { timeout } from 'rxjs/operators';
import { ApiResponse } from './order.service';

export interface ProductDetail {
  productId: number;
  title: string;
  category: string;
  barcode: string;
  image: string;
  status: string;
  originalValue: number;
  sellingPrice: number;
  quantity: number;
  weight: number;
  dimensions: string;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
/**
 * Coupling: Data coupling through a typed ProductDetail API response.
 * Cohesion: Functional cohesion because this service retrieves product details only.
 */
export class ProductService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/products';

  getProductDetail(productId: number): Observable<ApiResponse<ProductDetail>> {
    return this.http
      .get<ApiResponse<ProductDetail>>(`${this.apiUrl}/${productId}`)
      .pipe(timeout(8000));
  }
}
