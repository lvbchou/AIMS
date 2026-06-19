import { Routes } from '@angular/router';
import { CustomerLayoutComponent } from './layouts/customer-layout/customer-layout.component';
import { ManagerLayoutComponent } from './layouts/manager-layout/manager-layout.component';
import { ManagerDashboardComponent } from './features/dashboard/pages/manager-dashboard/manager-dashboard.component';
import { ProductManagementComponent } from './features/product/pages/product-management/product-management.component';
import { StockManagementComponent } from './features/stock/pages/stock-management/stock-management.component';
import { HomeComponent } from '../app/features/home/pages/home/home.component';
import { ProductListCustomerComponent } from './features/product/pages/product-list-customer/product-list-customer.component';
import { PaymentVietqrComponent } from './features/payment/pages/payment-vietqr/payment-vietqr.component';
import { PaymentRedirectComponent } from './features/payment/pages/payment-redirect/payment-redirect.component';
import { PaymentValidatingComponent } from './features/payment/pages/payment-validating/payment-validating.component';
import { PaymentSuccessComponent } from './features/payment/pages/payment-success/payment-success.component';
import { PaymentFailedComponent } from './features/payment/pages/payment-failed/payment-failed.component';
import { CartComponent } from './features/cart/pages/cart/cart.component';
import { DeliveryComponent } from './features/order/pages/delivery/delivery.component';
import { InvoiceComponent } from './features/order/pages/invoice/invoice.component';
import { PaymentResultComponent } from './features/payment/pages/payment-result/payment-result.component';
import { LoginComponent } from './features/auth/pages/login/login.component';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';

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
      },
      { 
        path: 'cart', 
        component: CartComponent 
      },
      { 
        path: '', 
        redirectTo: 'products', 
        pathMatch: 'full' 
      },
      { 
        path: 'delivery', 
        component: DeliveryComponent 
      },
      { 
        path: 'invoice', 
        component: InvoiceComponent 
      },
      {
        path: 'payment',
        pathMatch: 'full',
        redirectTo: 'payment/vietqr'
      },
      {
        path: 'payment/vietqr',
        component: PaymentVietqrComponent
      },
      {
        path: 'payment/redirect',
        component: PaymentRedirectComponent
      },
      {
        path: 'payment/validating',
        component: PaymentValidatingComponent
      },
      {
        path: 'payment/success',
        component: PaymentSuccessComponent
      },
      {
        path: 'payment/failed',
        component: PaymentFailedComponent
      },
      {
        path: 'payment/result',
        component: PaymentResultComponent
      }
    ]
  },

  // MANAGER
  {
    path: 'product-manager',
    canActivate: [authGuard, roleGuard],
    data: { role: 'PRODUCT_MANAGER' },
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
  },
  {
    path: 'login',
    component: LoginComponent
  },

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];