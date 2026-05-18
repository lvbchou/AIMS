import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header-customer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header-customer.component.html',
  styleUrls: ['./header-customer.component.scss']
})
export class HeaderCustomerComponent {
  cartCount = 0;

  constructor(private router: Router) {}

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
