import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { OrderService } from '../../services/order.service';
import { ProductDetail, ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-details.html',
  styleUrl: './product-details.css'
})
/**
 * Coupling: Data coupling with ProductService and OrderService through typed product/cart values.
 * Cohesion: Sequential cohesion because product retrieval precedes adding the selected item to cart.
 */
export class ProductDetails implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private changeDetectorRef = inject(ChangeDetectorRef);
  private productService = inject(ProductService);
  private orderService = inject(OrderService);

  product: ProductDetail | null = null;
  quantity = 1;
  isLoading = false;
  errorMsg = '';
  requestUrl = '';
  private loadingFallbackId: ReturnType<typeof setTimeout> | null = null;

  ngOnInit() {
    const productId = Number(this.route.snapshot.paramMap.get('id') || 1);
    this.requestUrl = `http://localhost:8080/api/products/${productId}`;
    this.isLoading = true;
    this.loadingFallbackId = setTimeout(() => {
      if (this.isLoading) {
        this.isLoading = false;
        this.errorMsg = `Product request timed out. Please check Backend API: ${this.requestUrl}`;
        this.changeDetectorRef.detectChanges();
      }
    }, 9000);

    this.productService.getProductDetail(productId).pipe(
      finalize(() => {
        this.clearLoadingFallback();
        this.isLoading = false;
        this.changeDetectorRef.detectChanges();
      })
    ).subscribe({
      next: (res) => {
        if (!res.success || !res.data) {
          this.errorMsg = res.message || 'Unable to load product details.';
          return;
        }
        this.product = res.data;
      },
      error: (err) => {
        this.errorMsg = err.error?.message || 'Unable to load product details.';
        console.error(err);
      }
    });
  }

  ngOnDestroy() {
    this.clearLoadingFallback();
  }

  addToCart() {
    if (!this.product) return;

    const stockQuantity = Number(this.product.quantity || 0);
    const selectedQuantity = Number(this.quantity || 0);

    if (stockQuantity < 1) {
      this.errorMsg = 'This product is out of stock.';
      return;
    }

    if (selectedQuantity < 1 || selectedQuantity > stockQuantity) {
      this.errorMsg = 'Quantity must be between 1 and current stock.';
      return;
    }

    this.orderService.addToCart({
      productId: this.product.productId,
      title: this.product.title,
      category: this.product.category,
      unitPriceExVat: this.product.sellingPrice,
      quantity: selectedQuantity,
      availableQuantity: stockQuantity,
      imageUrl: this.product.image || '/assets/book-cover.png'
    });
    this.router.navigate(['/cart']);
  }

  private clearLoadingFallback() {
    if (this.loadingFallbackId) {
      clearTimeout(this.loadingFallbackId);
      this.loadingFallbackId = null;
    }
  }
}
