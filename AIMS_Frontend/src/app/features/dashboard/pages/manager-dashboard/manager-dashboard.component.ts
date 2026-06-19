import { Component } from '@angular/core';
import { Router } from '@angular/router';
// import { FooterComponent } from '../../../../../shared/components/footer/footer.component';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './manager-dashboard.component.html',
  styleUrl: './manager-dashboard.component.scss'
})
export class ManagerDashboardComponent {

  constructor(private router: Router) {}

  navigate(section: string) {
    this.router.navigate(['/product-manager', section]);
  }

  getUsername(): string {
    // In a real application, you would fetch the username from an authentication service or user profile
    return localStorage.getItem('username') || 'Manager';
  }
}