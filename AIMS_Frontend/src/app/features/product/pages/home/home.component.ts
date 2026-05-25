import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProductCardComponent } from '../../components/product-card/product-card.component';
import { ProductDetailModalComponent } from '../../components/product-detail-modal/product-detail-modal.component';
import { ProductService } from '../../services/product.service';
import { ProductSummary } from '../../models/product.model';
import { MOCK_SUMMARIES } from '../../data/mock-products.data';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, ProductDetailModalComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  products: ProductSummary[] = [];
  selectedProductId: number | null = null;

  constructor(private api: ProductService, private router: Router) {}

  ngOnInit(): void {
  this.products = MOCK_SUMMARIES;
  this.api.getAll().subscribe({
    next: (res) => { if (res?.length) this.products = res.slice(0, 20); },
    error: () => {}
  });
}

  onViewDetail(id: number): void  { this.selectedProductId = id; }
  onAddToCart(id: number): void   { console.log('Add to cart id:', id); }
  closeModal(): void               { this.selectedProductId = null; }
  viewAll(): void                  { this.router.navigate(['/products']); }
}
