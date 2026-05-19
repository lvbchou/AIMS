import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderCustomerComponent } from '../../core/components/header-customer/header-customer.component';
import { FooterComponent } from '../../core/components/footer/footer.component';

@Component({
  selector: 'app-customer-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderCustomerComponent, FooterComponent],
  templateUrl: './customer-layout.component.html'
})
export class CustomerLayoutComponent {}