import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-header-manager',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header-manager.component.html',
  styleUrls: ['./header-manager.component.scss']
})
export class HeaderManagerComponent {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  navItems = [
    { label: 'Products', route: '/product-manager/products' },
    { label: 'Stock',    route: '/product-manager/stock' },
    { label: 'Orders',   route: '/product-manager/orders' },
  ];

  accountOpen = false;
  accountMenuItems = [
    { label: 'Profile',         route: '/product-manager/profile',          action: () => console.log('Profile clicked') },
    { label: 'Change Password', route: null,                                 action: () => this.goToChangePassword() },
    { label: 'Settings',        route: '/product-manager/settings',          action: () => console.log('Settings clicked') },
    { label: 'Logout',          route: '/login',                             action: () => this.onLogout() },
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

  onLogout(): void {
    this.authService.logout();
  }

  goToChangePassword(): void {
    if (this.router.url.startsWith('/admin')) {
      this.router.navigate(['/admin/change-password']);
    } else {
      this.router.navigate(['/product-manager/change-password']);
    }
  }
}