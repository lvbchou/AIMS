import { Routes } from '@angular/router';
import { CustomerLayoutComponent } from './layouts/customer-layout/customer-layout.component';
import { ManagerLayoutComponent } from './layouts/manager-layout/manager-layout.component';
import { ManagerDashboardComponent } from './features/dashboard/pages/manager-dashboard/manager-dashboard.component';
import { ProductManagementComponent } from './features/product/pages/product-management/product-management.component';
import { StockManagementComponent } from './features/stock/pages/stock-management/stock-management.component';
import { HomeComponent } from '../app/features/home/pages/home/home.component';
import { ProductListCustomerComponent } from './features/product/pages/product-list-customer/product-list-customer.component';
import { PaymentVietqrComponent } from './features/payment/methods/vietqr/pages/vietqr-scan/payment-vietqr.component';
import { PaymentRedirectComponent } from './features/payment/methods/paypal/pages/paypal-redirect/payment-redirect.component';
import { PaymentValidatingComponent } from './features/payment/methods/vietqr/pages/vietqr-validating/payment-validating.component';
import { PaymentSuccessComponent } from './features/payment/core/pages/payment-success/payment-success.component';
import { PaymentFailedComponent } from './features/payment/core/pages/payment-failed/payment-failed.component';
import { CartComponent } from './features/cart/pages/cart/cart.component';
import { DeliveryComponent } from './features/order/pages/delivery/delivery.component';
import { InvoiceComponent } from './features/order/pages/invoice/invoice.component';
import { PaymentResultComponent } from './features/payment/methods/paypal/pages/paypal-result/payment-result.component';
import { LoginComponent } from './features/auth/pages/login/login.component';
import { ChangePasswordComponent } from './features/auth/pages/change-password/change-password.component';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';
import { OrderCancelComponent } from './features/order/pages/cancel/cancel.component';

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
      },
      {
        path: 'orders/:orderId/cancel',
        component: OrderCancelComponent
      }
    ]
  },

  // MANAGER
  {
    path: 'product-manager',
    canActivate: [authGuard, roleGuard],
    data: { role: 'ROLE_PRODUCT_MANAGER' },
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
      },
      {
        // UC-CP: Change Password — protected under manager layout so the
        // header dropdown is rendered on top, giving the modal-overlay effect.
        path: 'change-password',
        component: ChangePasswordComponent,
        canActivate: [authGuard]
      }
    ]
  },
  // ADMIN
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { role: 'ADMIN' },
    component: ManagerLayoutComponent,
    children: [
      {
        path: '',
        component: ManagerDashboardComponent
      },
      {
        path: 'change-password',
        component: ChangePasswordComponent,
        canActivate: [authGuard]
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