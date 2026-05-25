import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product, ProductSummary } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';
import { ProductListComponent } from '../../components/product-list/product-list.component';
import { ProductFormComponent } from '../../components/product-form/product-form.component';
import { ToastService } from '../../../../core/services/toast.service';
import { DialogService } from '../../../../core/services/dialog.service';
import { ConfirmDialogComponent } from '../../../../shared/components/dialog/confirm-dialog/confirm-dialog.component';
import { ToastComponent } from '../../../../shared/components/toast/toast/toast.component';
import { ProductService } from '../../services/product.service';
import { ManagerProductViewModalComponent } from '../../components/manager-product-view-modal/manager-product-view-modal.component';

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, ProductListComponent, ProductFormComponent, ConfirmDialogComponent, ToastComponent, ManagerProductViewModalComponent],
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.scss'
})
export class ProductManagementComponent implements OnInit {

  constructor(
    private dialogService: DialogService,
    private toastService: ToastService,
    private productService: ProductService,
    private cdr: ChangeDetectorRef
  ) {}

  products: ProductSummary[] = [];
  selectionMode = false;
  selectedIds: Set<number> = new Set();
  showProductForm = false;
  selectedProduct: Product | null = null;
  isLoading = false;
  viewDetailProduct: Product | null = null;

  ngOnInit(): void {
    this.isLoading = true;
    this.productService.getAll().subscribe({
      next: (data) => {
        this.products = data;
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

  get isMaxSelected(): boolean {
    return this.selectedIds.size >= 10;
  }

  enterSelectionMode() {
    this.selectionMode = true;
  }

  cancelSelection() {
    this.selectionMode = false;
    this.selectedIds = new Set();
  }

  onToggle(productId: number) {
    if (this.selectedIds.has(productId)) {
      this.selectedIds.delete(productId);
    } else {
      this.selectedIds.add(productId);
      if (this.selectedIds.size == 10) {
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
    if (this.selectedProduct) {
      this.productService.update(product.productId, product).subscribe({
        next: () => {
          this.products = this.products.map(p =>
            p.productId === product.productId
              ? { ...p, title: product.title, sellingPrice: product.sellingPrice, imageUrl: (product as any).imageUrl }
              : p
          );
          this.showProductForm = false;
          this.selectedProduct = null;
          this.toastService.show('Product updated successfully');
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.toastService.show('Failed to update product. Please try again later.');
          this.cdr.detectChanges();
        }
      });
    } else {
      this.productService.add(product).subscribe({
        next: (created) => {
          this.products = [...this.products, created];
          this.showProductForm = false;
          this.toastService.show('Product created successfully');
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

  onCancel(): void {
    this.showProductForm = false;
    this.selectedProduct = null;
  }

  onUpdate(productId: number) {
    this.productService.getById(productId).subscribe({
      next: (data) => {
        this.selectedProduct = data;
        this.showProductForm = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  onDelete(productId: number) {
    const product = this.products.find(p => p.productId === productId);
    this.dialogService.open({
      title: 'Confirm Delete',
      message: `Are you sure you want to delete "${product?.title}"?`,
      onConfirm: () => {
        if (!product) return;
        this.productService.delete(product.productId).subscribe({
          next: () => {
            this.products = this.products.filter(p => p.productId !== productId);
            this.toastService.show('Delete product successfully');
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

  /* onViewDetail(productId: number): void {
    const s = this.products.find(p => p.productId === productId);
    if (!s) return;
    this.viewDetailProduct = this.buildMockFullProduct(s);
  }

  closeViewDetail(): void {
    this.viewDetailProduct = null;
  }
  */

   // Thêm field loading/error cho modal
  // XÓA buildMockFullProduct() và sửa lại 2 hàm này:

  onViewDetail(productId: number): void {
    this.productService.getById(productId).subscribe({
      next: (product) => {
        this.viewDetailProduct = product;
      },
      error: () => {
        this.toastService.show('Failed to load product details. Please try again.');
      }
    });
  }

  closeViewDetail(): void {
    this.viewDetailProduct = null;
  }



  /* private buildMockFullProduct(s: ProductSummary): any {
    const base = {
      productId: s.productId,
      productType: s.productType,
      title: s.title,
      category: String(s.productType),
      barcode: '893000000' + String(s.productId).padStart(4, '0'),
      imageUrl: s.imageUrl ?? s.image ?? '',
      originalValue: Math.round(s.sellingPrice * 0.75),
      sellingPrice: s.sellingPrice,
      weight: 0.3,
      dimensions: '20 × 15 × 2 cm',
      description: "Quality media product from AIMS Store. Vietnam's best selection.",
    };
    switch (s.productType) {
      case ProductType.DVD:
        return { ...base, type: ProductType.DVD, typeDetails: { discType: 'DVD', director: 'John Carter', runtime: 142, studio: 'Warner Bros', language: 'English', subtitles: 'EN/VI', genre: 'Sci-fi', releaseDate: '2023-05-12' }};
      case ProductType.CD:
        return { ...base, type: ProductType.CD, typeDetails: { artists: ['The Artist'], recordLabel: 'Sony Music', genre: 'Indie', releaseDate: '2023-09-01', tracklist: [{ title: 'Silent Dawn', length: '3:24' }, { title: 'Echoes', length: '4:10' }, { title: 'Faded', length: '3:55' }] }};
      case ProductType.BOOK:
        return { ...base, type: ProductType.BOOK, typeDetails: { author: 'Author Name', coverType: 'Paperback', pages: 244, genre: 'Literary Fiction', publisher: 'NXB Tre', publicationDate: '2022-10-12', language: 'Vietnamese' }};
      case ProductType.NEWSPAPER:
        return { ...base, type: ProductType.NEWSPAPER, typeDetails: { editorInChief: 'Le The Chu', issueNumber: '18234', publicationFrequency: 'daily', issn: '1859-1207', publisher: 'Tuoi Tre', publicationDate: '2024-05-11', language: 'Vietnamese', sections: ['News', 'Sports'] }};
      default:
        return base;
    }
  }
  */
}