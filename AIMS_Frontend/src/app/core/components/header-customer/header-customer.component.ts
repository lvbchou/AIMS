import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { CartService } from '../../../features/cart/services/cart.service';

@Component({
  selector: 'app-header-customer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header-customer.component.html',
  styleUrls: ['./header-customer.component.scss']
})
export class HeaderCustomerComponent implements OnInit, OnDestroy {
  cartCount = 0;
  private destroy$ = new Subject<void>();

  constructor(
    private router:      Router,
    private cartService: CartService,
    private cdr:         ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    // Subscribe cart$ để tự động cập nhật khi cart thay đổi
    this.cartService.cart$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.cartCount = this.cartService.getDifferentItemCount();
        this.cdr.detectChanges();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setActive(event: MouseEvent) {
    event.preventDefault();
    const target = event.currentTarget as HTMLElement;
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    target.classList.add('active');
  }

  onSearch(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      const input = event.target as HTMLInputElement;
      const kw = input.value.trim();
      kw ? this.router.navigate(['/products'], { queryParams: { q: kw } })
         : this.router.navigate(['/products']);
    }
  }
}
