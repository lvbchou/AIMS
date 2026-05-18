import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-header-manager',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header-manager.component.html',
  styleUrls: ['./header-manager.component.scss']
})
export class HeaderManagerComponent {
  navItems = [
    { label: 'Products', route: '/product-manager/products' },
    { label: 'Stock',    route: '/product-manager/stock' },
    { label: 'Orders',   route: '/product-manager/orders' },
  ];

  accountOpen = false;
  accountMenuItems = [
    { label: 'Profile',  route: '/product-manager/profile' },
    { label: 'Settings', route: '/product-manager/settings' },
    { label: 'Logout',   route: '/logout' },
  ];

  toggleAccount() {
    this.accountOpen = !this.accountOpen;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!(event.target as HTMLElement).closest('.account-menu')) {
      this.accountOpen = false;
    }
  }
}