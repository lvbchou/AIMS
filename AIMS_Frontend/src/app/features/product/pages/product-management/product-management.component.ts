import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product, ProductSummary } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { ProductFormComponent } from '../../components/product-form/product-form.component';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { DialogService } from '../../../../core/services/dialog/dialog.service';
import { ConfirmDialogComponent } from '../../../../shared/components/dialog/confirm-dialog/confirm-dialog.component';
import { ToastComponent } from '../../../../shared/components/toast/toast/toast.component';
import { ProductService } from '../../services/product.service';
import { ManagerProductViewModalComponent } from '../../components/manager-product-view-modal/manager-product-view-modal.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntil } from 'rxjs/internal/operators/takeUntil';

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, ProductListComponent, ProductFormComponent, ConfirmDialogComponent, ToastComponent, ManagerProductViewModalComponent, PaginationComponent],
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.scss'
})
export class ProductManagementComponent implements OnInit {

  constructor(
    private dialogService: DialogService,
    private toastService: ToastService,
    private productService: ProductService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  products: ProductSummary[] = [];
  selectionMode = false;
  selectedIds: Set<number> = new Set();
  showProductForm = false;
  selectedProduct: Product | null = null;
  isLoading = false;
  viewDetailProduct: Product | null = null;
  currentPage = 0;
  pageSize    = 10;
  totalPages  = 0;
  totalElements = 0;

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.currentPage = (params['page'] ?? 1) - 1;
      this.loadProducts();
    });
  }

  get isMaxSelected(): boolean {
    return this.selectedIds.size >= 10;
  }

  enterSelectionMode() {
    this.selectionMode = true;
  }

  private loadProducts() {
    this.isLoading = true;
    this.productService.getAll(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        console.log('Data received:', data); //console log để kiểm tra dữ liệu nhận được từ API
        this.products = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: page + 1 },
      queryParamsHandling: 'merge',
    });
  }

  cancelSelection() {
    this.selectionMode = false;
    this.selectedIds = new Set();
  }

  onToggle(productId: number) {
    if (this.selectedIds.has(productId)) {
      this.selectedIds.delete(productId);
    }
    else {
      this.selectedIds.add(productId);
      if (this.selectedIds.size == 10) {
        console.log(this.selectedIds.size);
        this.toastService.show('Reach maximum selection of 10 products at once');
        return;
      }
    }
    this.selectedIds = new Set(this.selectedIds);
    this.selectionMode = this.selectedIds.size > 0;
  }

  onAdd() {
    this.selectedProduct = null;
    this.showProductForm = true;
  }

  onFormSubmitted(product: Product) {
    // TODO: gọi service save
    if (this.selectedProduct) {
      console.log('Updating product with data:', product);
      this.productService.update(product).subscribe({
        next: () => {
          this.showProductForm = false;
          this.selectedProduct = null;
          this.toastService.show('Product updated successfully');
          this.loadProducts();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.toastService.show('Failed to update product. Please try again later.');
          this.cdr.detectChanges();
        }
      });
    }
    else {
      this.productService.add(product).subscribe({
        next: (created) => {
          // this.products = [...this.products, created];
          this.showProductForm = false;
          this.toastService.show('Product created successfully');
          this.loadProducts();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.toastService.show('Failed to create product. Please try again later.');
          this.cdr.detectChanges();
        }
      });
    }

  }

  onCreateCancelled() {
    this.showProductForm = false;
  }

  onUpdate(productId: number) {
    // TODO: navigate hoặc mở popup update
    this.productService.getById(productId).subscribe({
      next: (data) => {
        this.selectedProduct = data;console.log('Selected product for update:', this.selectedProduct);
        this.showProductForm = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  onDelete(product: ProductSummary) {
    this.dialogService.open({
      title: 'Confirm Delete',
      message: `Are you sure you want to delete "${product?.title}"?`,
      onConfirm: () => {
        this.productService.delete(product.productId).subscribe({
          next: () => {
            this.products = this.products.filter(p => p.productId !== p.productId);
            this.toastService.show('Delete product successfully');
            this.loadProducts();
          },

          error: (err) => {
            console.error(err);
            this.toastService.show('Failed to delete product. Please try again later.');
          }
        });
      }
    });
  }

  deleteSelected() {
    const count = this.selectedIds.size;
    this.dialogService.open({
      title: 'Delete Selected',
      message: `Delete ${count} selected products?`,
      onConfirm: () => {
        this.productService.deleteMany(Array.from(this.selectedIds)).subscribe({
          next: () => {
            this.products = this.products.filter(p => !this.selectedIds.has(p.productId));
            this.selectedIds = new Set();
            this.selectionMode = false;
            this.toastService.show(`Deleted ${count} products`);
            this.loadProducts();
          },
          error: (err) => {
            console.error(err);
            this.selectedIds = new Set();
            this.selectionMode = false;
            this.toastService.show('Failed to delete products. Please try again later.');
          }
        });
      }
    });
  }

  onViewDetail(productId: number): void {
    this.productService.getById(productId).subscribe({
      next: (product) => {
        console.log('Product detail:', product);
        this.viewDetailProduct = product;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.toastService.show('Failed to load product detail');
      }
    });
  }

  closeViewDetail(): void { this.viewDetailProduct = null; }
}