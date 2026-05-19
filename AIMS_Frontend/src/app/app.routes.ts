import { Routes } from '@angular/router';
import { CustomerLayoutComponent } from './layouts/customer-layout/customer-layout.component';
import { ManagerLayoutComponent } from './layouts/manager-layout/manager-layout.component';
import { ManagerDashboardComponent } from './features/dashboard/pages/manager-dashboard/manager-dashboard.component';
import { ProductManagementComponent } from './features/product/pages/product-management/product-management.component';
import { StockManagementComponent } from './features/stock/pages/stock-management/stock-management.component';
import { HomeComponent } from './features/product/pages/home/home.component';
import { ProductListCustomerComponent } from './features/product/pages/product-list-customer/product-list-customer.component';

export const routes: Routes = [

  // CUSTOMER
  {
    path: '',
    component: CustomerLayoutComponent,
    children: [
      {
        path: '',
        component: HomeComponent
      },
      {
        path: 'products',
        component: ProductListCustomerComponent
      }
    ]
  },

  // MANAGER
  {
    path: 'product-manager',
    component: ManagerLayoutComponent,
    children: [
      {
        path: '',
        component: ManagerDashboardComponent
      },
      {
        path: 'products',
        component: ProductManagementComponent
      },
      {
        path: 'stock',
        component: StockManagementComponent
      }
    ]
  }

];