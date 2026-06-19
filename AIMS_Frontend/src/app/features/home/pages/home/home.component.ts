import { Component, OnInit } from '@angular/core';
import { CommonModule, AsyncPipe } from '@angular/common';
import { Router } from '@angular/router';

import { Product } from '../../../product/models/product.model';
import { ProductDetailModalComponent } from '../../../product/components/product-detail-modal/product-detail-modal.component';
import { ProductListComponent } from '../../../product/components/product-list/product-list.component';
import { ToastComponent } from '../../../../shared/components/toast/toast/toast.component';

import { ProductListFacade } from '../../../product/facades/product-list.facade';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { CartService } from '../../../cart/services/cart.service';
import { BehaviorSubject, Subject } from 'rxjs';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    AsyncPipe,
    ProductDetailModalComponent,
    ProductListComponent,
    ToastComponent,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  providers: [
    // ProductListFacade dùng ActivatedRoute nên cần provide tại đây
    // nếu home không nằm trong router-outlet có route riêng.
    // Nếu đã có route thì xoá dòng providers này.
    ProductListFacade,
  ],
})
export class HomeComponent implements OnInit {

  private readonly _viewProductDetail$ = new BehaviorSubject<Product | null>(null);
  readonly viewProductDetail$ = this._viewProductDetail$.asObservable();

  constructor(
    readonly listFacade: ProductListFacade,
    private router: Router,
    private toastService: ToastService,
    private cartService: CartService,
  ) {}

  ngOnInit(): void {
    this.listFacade.initFromRoute();
  }

  onViewDetail(id: number): void {
    this.listFacade.getById(id).subscribe({
      next:  (product) => { 
        this._viewProductDetail$.next(product);
      },
      error: ()        => { this.toastService.show('Failed to load product details'); },
    });
  }

  onAddToCart(id: number): void {
    this.cartService.addToCart(id, 1);
    this.toastService.show('Added to cart');
  }

  get products$() {
    return this.listFacade.products$;
  }

  get isLoading$() {
    return this.listFacade.isLoading$;
  }

  closeModal(): void {
    this._viewProductDetail$.next(null);
  }

  viewAll(): void {
    this.router.navigate(['/products']);
  }
}