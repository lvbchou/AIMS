import { Routes } from '@angular/router';
import { CustomerLayoutComponent } from './layouts/customer-layout/customer-layout.component';
import { ManagerLayoutComponent } from './layouts/manager-layout/manager-layout.component';
import { ManagerDashboardComponent } from './features/dashboard/pages/manager-dashboard/manager-dashboard.component';
import { ProductManagementComponent } from './features/product/pages/product-management/product-management.component';
import { StockManagementComponent } from './features/stock/pages/stock-management/stock-management.component';
import { HomeComponent } from './features/product/pages/home/home.component';
import { ProductListCustomerComponent } from './features/product/pages/product-list-customer/product-list-customer.component';
import { PaymentVietqrComponent } from './features/payment/pages/payment-vietqr/payment-vietqr.component';
import { PaymentRedirectComponent } from './features/payment/pages/payment-redirect/payment-redirect.component';
import { PaymentValidatingComponent } from './features/payment/pages/payment-validating/payment-validating.component';
import { PaymentSuccessComponent } from './features/payment/pages/payment-success/payment-success.component';
import { PaymentFailedComponent } from './features/payment/pages/payment-failed/payment-failed.component';

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
  },
  }

]; import { Routes } from '@angular/router';
import { CustomerLayoutComponent } from './layouts/customer-layout/customer-layout.component';
import { ManagerLayoutComponent } from './layouts/manager-layout/manager-layout.component';
import { ManagerDashboardComponent } from './features/dashboard/pages/manager-dashboard/manager-dashboard.component';
import { ProductManagementComponent } from './features/product/pages/product-management/product-management.component';
import { StockManagementComponent } from './features/stock/pages/stock-management/stock-management.component';
import { HomeComponent } from './features/product/pages/home/home.component';
import { ProductListCustomerComponent } from './features/product/pages/product-list-customer/product-list-customer.component';
import { Cart } from './pages/cart/cart';
import { Delivery } from './pages/delivery/delivery';
import { Invoice } from './pages/invoice/invoice';
import { Success } from './pages/success/success';
import { ProductDetails } from './pages/product-details/product-details';
import { Payment } from './pages/payment/payment';

  // PAYMENT
  {
    path: 'payment',
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'vietqr'
      },
      {
        path: 'vietqr',
        component: PaymentVietqrComponent
      },
      {
        path: 'redirect',
        component: PaymentRedirectComponent
      },
      {
        path: 'validating',
        component: PaymentValidatingComponent
      },
      {
        path: 'success',
        component: PaymentSuccessComponent
      },
      {
        path: 'failed',
        component: PaymentFailedComponent
      }
    ]
  }
export const routes: Routes = [
  { path: '', redirectTo: 'products/1', pathMatch: 'full' },
  { path: 'products/:id', component: ProductDetails },
  { path: 'cart', component: Cart },
  { path: 'delivery', component: Delivery },
  { path: 'invoice', component: Invoice },
  { path: 'payment', component: Payment },
  { path: 'success', component: Success }
];


];
