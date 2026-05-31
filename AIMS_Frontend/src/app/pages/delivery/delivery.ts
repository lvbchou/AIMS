import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Stepper } from '../../components/stepper/stepper';
import { CartItemRequest, DeliveryInfoRequest, OrderService } from '../../services/order.service';
import { finalize, switchMap } from 'rxjs';

@Component({
  selector: 'app-delivery',
  standalone: true,
  imports: [CommonModule, FormsModule, Stepper],
  templateUrl: './delivery.html',
  styleUrl: './delivery.css'
})
/**
 * Coupling: Data coupling with OrderService; workflow chaining reflects the backend session contract.
 * Cohesion: Sequential cohesion because delivery validation leads to shipping and invoice requests.
 */
export class Delivery implements OnInit {
  private orderService = inject(OrderService);
  private router = inject(Router);
  private changeDetectorRef = inject(ChangeDetectorRef);

  deliveryInfo: DeliveryInfoRequest = {
    recipientName: '',
    phoneNumber: '',
    email: '',
    deliveryProvince: '',
    detailAddress: '',
    note: ''
  };

  subtotal = 0;
  vat = 0;
  shippingFee: number | null = null;
  errorMsg = '';
  isLoading = false;

  ngOnInit() {
    const cart = this.orderService.getCurrentCart();
    if (cart.length === 0) {
      this.router.navigate(['/cart']);
      return;
    }
    this.subtotal = cart.reduce((sum, item) => sum + item.unitPriceExVat * item.quantity, 0);
    this.vat = Math.round(this.subtotal * 0.1);
  }

  get totalPayable(): number {
    return this.subtotal + this.vat + (this.shippingFee || 0);
  }

  onProvinceChange() {
    if (!this.deliveryInfo.deliveryProvince) return;

    const requestItems = this.getCartRequestItems();
    if (requestItems.length === 0) {
      this.router.navigate(['/cart']);
      return;
    }

    this.orderService.placeOrder(requestItems).pipe(
      switchMap(() => this.orderService.calculateShipping(this.deliveryInfo))
    ).subscribe({
      next: (res) => {
        if (res.success) {
          this.shippingFee = res.data;
        }
      },
      error: (err) => {
        this.shippingFee = null;
        this.errorMsg = err.error?.message || 'An error occurred while calculating shipping fee.';
        console.error(err);
      }
    });
  }

  reviewInvoice() {
    if (!this.deliveryInfo.recipientName || !this.deliveryInfo.phoneNumber || !this.deliveryInfo.deliveryProvince || !this.deliveryInfo.detailAddress) {
      this.errorMsg = 'Please fill all required fields: full name, phone number, city/province, and detailed address.';
      return;
    }

    this.isLoading = true;
    this.errorMsg = '';
    const requestItems = this.getCartRequestItems();
    if (requestItems.length === 0) {
      this.isLoading = false;
      this.router.navigate(['/cart']);
      return;
    }

    this.orderService.placeOrder(requestItems).pipe(
      switchMap(() => this.orderService.createInvoice(this.deliveryInfo)),
      finalize(() => {
        this.isLoading = false;
        this.changeDetectorRef.detectChanges();
      })
    ).subscribe({
      next: (res) => {
        if (res.success) {
          this.orderService.setCurrentDeliveryInfo(this.deliveryInfo);
          this.orderService.setCurrentInvoice(res.data);
          this.router.navigate(['/invoice']);
        } else {
          this.errorMsg = res.message;
        }
      },
      error: (err) => {
        this.errorMsg = err.error?.message || err.message || 'An error occurred while creating the invoice.';
        console.error(err);
      }
    });
  }

  private getCartRequestItems(): CartItemRequest[] {
    return this.orderService.getCurrentCart().map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }));
  }
}
