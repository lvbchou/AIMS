import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProductDetailModalComponent } from '../../components/product-detail-modal/product-detail-modal.component';
import { ProductService } from '../../services/product.service';
import { Product, ProductSummary } from '../../models/product.model';
import { MOCK_SUMMARIES } from '../../data/mock-products.data';
import { ChangeDetectorRef } from '@angular/core';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { ToastComponent } from '../../../../shared/components/toast/toast/toast.component';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { CartService } from '../../../cart/services/cart.service';


@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ProductDetailModalComponent, ToastComponent, ProductListComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  products: ProductSummary[] = [];
  viewProductDetail: Product | null = null;
  currentPage = 0;
  pageSize    = 10;
  totalPages  = 0;
  totalElements = 0;
  isLoading = false;

  constructor(
    private api: ProductService, 
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastService: ToastService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    // this.api.search('').subscribe({
    //   next: (res) => { if (res?.length) this.products = res; },
    //   error: () => {}
    // });
  }

  private loadProducts() {
    this.isLoading = true;
    this.api.getAll(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.products = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onViewDetail(id: number): void  {
    this.api.getById(id).subscribe({
      next: (product) => {
        this.viewProductDetail = product;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.toastService.show("Failed to load product details");
      }
    });
  }
  onAddToCart(id: number): void {
    this.cartService.addToCart(id, 1);
    this.toastService.show('Added to cart');
  }

  closeModal(): void {
    this.viewProductDetail = null;
  }

  viewAll(): void { 
    this.router.navigate(['/products']);
  }
}
