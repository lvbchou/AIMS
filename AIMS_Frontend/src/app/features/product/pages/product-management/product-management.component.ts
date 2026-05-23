import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Component, OnInit } from '@angular/core';
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
    private productService: ProductService
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

    // setTimeout(() => {
    //   this.products = [
    //     { productId: 1,  title: 'The Last Horizon',           productType: ProductType.DVD,       sellingPrice: 120000, imageUrl: 'https://picsum.photos/seed/p1/400/400' },
    //     { productId: 2,  title: 'Echoes of Silence',           productType: ProductType.CD,        sellingPrice: 185000, imageUrl: 'https://picsum.photos/seed/p2/400/400' },
    //     { productId: 3,  title: 'Midnight Street',             productType: ProductType.DVD,       sellingPrice: 150000, imageUrl: 'https://picsum.photos/seed/p3/400/400' },
    //     { productId: 4,  title: 'Prism Theory',                productType: ProductType.DVD,       sellingPrice: 110000, imageUrl: 'https://picsum.photos/seed/p4/400/400' },
    //     { productId: 5,  title: 'Tuoi Tre',                    productType: ProductType.NEWSPAPER, sellingPrice: 8000,   imageUrl: 'https://picsum.photos/seed/p5/400/400' },
    //     { productId: 6,  title: 'The New York Times - Int\'l', productType: ProductType.NEWSPAPER, sellingPrice: 45000,  imageUrl: 'https://picsum.photos/seed/p6/400/400' },
    //     { productId: 7,  title: 'The Art of Minimalist Living',productType: ProductType.BOOK,      sellingPrice: 145000, imageUrl: 'https://picsum.photos/seed/p7/400/400' },
    //     { productId: 8,  title: 'Pop Hits 2023 - Various',     productType: ProductType.CD,        sellingPrice: 120000, imageUrl: 'https://picsum.photos/seed/p8/400/400' },
    //     { productId: 9,  title: 'Jazz Classics: Midnight Soul', productType: ProductType.CD,       sellingPrice: 110000, imageUrl: 'https://picsum.photos/seed/p9/400/400' },
    //     { productId: 10, title: 'Interstellar Special Edition', productType: ProductType.DVD,      sellingPrice: 189000, imageUrl: 'https://picsum.photos/seed/p10/400/400' },
    //     { productId: 11, title: 'Wild Earth',                  productType: ProductType.DVD,       sellingPrice: 130000, imageUrl: 'https://picsum.photos/seed/p11/400/400' },
    //     { productId: 12, title: 'Shadow Alley',                productType: ProductType.DVD,       sellingPrice: 85000,  imageUrl: 'https://picsum.photos/seed/p12/400/400' },
    //     { productId: 13, title: 'Tuoi Tho Du Doi',             productType: ProductType.BOOK,      sellingPrice: 95000,  imageUrl: 'https://picsum.photos/seed/p13/400/400' },
    //     { productId: 14, title: 'Atomic Habits',               productType: ProductType.BOOK,      sellingPrice: 165000, imageUrl: 'https://picsum.photos/seed/p14/400/400' },
    //     { productId: 15, title: 'Sapiens: A Brief History',    productType: ProductType.BOOK,      sellingPrice: 220000, imageUrl: 'https://picsum.photos/seed/p15/400/400' },
    //     { productId: 16, title: 'Tuoi Tre Newspaper',          productType: ProductType.NEWSPAPER, sellingPrice: 8000,   imageUrl: 'https://picsum.photos/seed/p16/400/400' },
    //     { productId: 17, title: 'Thanh Nien Weekly',           productType: ProductType.NEWSPAPER, sellingPrice: 15000,  imageUrl: 'https://picsum.photos/seed/p17/400/400' },
    //     { productId: 18, title: 'Classical Symphony Vol.1',    productType: ProductType.CD,        sellingPrice: 175000, imageUrl: 'https://picsum.photos/seed/p18/400/400' },
    //     { productId: 19, title: 'Rock Anthems Greatest',       productType: ProductType.CD,        sellingPrice: 135000, imageUrl: 'https://picsum.photos/seed/p19/400/400' },
    //     { productId: 20, title: 'Inception (2-Disc)',          productType: ProductType.DVD,       sellingPrice: 210000, imageUrl: 'https://picsum.photos/seed/p20/400/400' },
    //   ];
    //   this.isLoading = false;
    // }, 1500);
    this.productService.getAll().subscribe({
      next: (data) => {
        console.log('Data received:', data);
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
    setTimeout(() => {
      this.products = [
        { productId: 1,  title: 'The Last Horizon',           productType: ProductType.DVD,       sellingPrice: 120000, imageUrl: 'https://picsum.photos/seed/p1/400/400' },
        { productId: 2,  title: 'Echoes of Silence',           productType: ProductType.CD,        sellingPrice: 185000, imageUrl: 'https://picsum.photos/seed/p2/400/400' },
        { productId: 3,  title: 'Midnight Street',             productType: ProductType.DVD,       sellingPrice: 150000, imageUrl: 'https://picsum.photos/seed/p3/400/400' },
        { productId: 4,  title: 'Prism Theory',                productType: ProductType.DVD,       sellingPrice: 110000, imageUrl: 'https://picsum.photos/seed/p4/400/400' },
        { productId: 5,  title: 'Tuoi Tre',                    productType: ProductType.NEWSPAPER, sellingPrice: 8000,   imageUrl: 'https://picsum.photos/seed/p5/400/400' },
        { productId: 6,  title: 'The New York Times - Int\'l', productType: ProductType.NEWSPAPER, sellingPrice: 45000,  imageUrl: 'https://picsum.photos/seed/p6/400/400' },
        { productId: 7,  title: 'The Art of Minimalist Living',productType: ProductType.BOOK,      sellingPrice: 145000, imageUrl: 'https://picsum.photos/seed/p7/400/400' },
        { productId: 8,  title: 'Pop Hits 2023 - Various',     productType: ProductType.CD,        sellingPrice: 120000, imageUrl: 'https://picsum.photos/seed/p8/400/400' },
        { productId: 9,  title: 'Jazz Classics: Midnight Soul', productType: ProductType.CD,       sellingPrice: 110000, imageUrl: 'https://picsum.photos/seed/p9/400/400' },
        { productId: 10, title: 'Interstellar Special Edition', productType: ProductType.DVD,      sellingPrice: 189000, imageUrl: 'https://picsum.photos/seed/p10/400/400' },
        { productId: 11, title: 'Wild Earth',                  productType: ProductType.DVD,       sellingPrice: 130000, imageUrl: 'https://picsum.photos/seed/p11/400/400' },
        { productId: 12, title: 'Shadow Alley',                productType: ProductType.DVD,       sellingPrice: 85000,  imageUrl: 'https://picsum.photos/seed/p12/400/400' },
        { productId: 13, title: 'Tuoi Tho Du Doi',             productType: ProductType.BOOK,      sellingPrice: 95000,  imageUrl: 'https://picsum.photos/seed/p13/400/400' },
        { productId: 14, title: 'Atomic Habits',               productType: ProductType.BOOK,      sellingPrice: 165000, imageUrl: 'https://picsum.photos/seed/p14/400/400' },
        { productId: 15, title: 'Sapiens: A Brief History',    productType: ProductType.BOOK,      sellingPrice: 220000, imageUrl: 'https://picsum.photos/seed/p15/400/400' },
        { productId: 16, title: 'Tuoi Tre Newspaper',          productType: ProductType.NEWSPAPER, sellingPrice: 8000,   imageUrl: 'https://picsum.photos/seed/p16/400/400' },
        { productId: 17, title: 'Thanh Nien Weekly',           productType: ProductType.NEWSPAPER, sellingPrice: 15000,  imageUrl: 'https://picsum.photos/seed/p17/400/400' },
        { productId: 18, title: 'Classical Symphony Vol.1',    productType: ProductType.CD,        sellingPrice: 175000, imageUrl: 'https://picsum.photos/seed/p18/400/400' },
        { productId: 19, title: 'Rock Anthems Greatest',       productType: ProductType.CD,        sellingPrice: 135000, imageUrl: 'https://picsum.photos/seed/p19/400/400' },
        { productId: 20, title: 'Inception (2-Disc)',          productType: ProductType.DVD,       sellingPrice: 210000, imageUrl: 'https://picsum.photos/seed/p20/400/400' },
      ];
      this.isLoading = false;
    }, 1500);
    // this.productService.getAll().subscribe({
    //   next: (data) => this.products = data,
    //   error: (err) => console.error(err)
    // });
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
          this.products = this.products.map(p => p.productId === product.productId ? { ...p, title: product.title, sellingPrice: product.sellingPrice, image: product.image } : p);
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
    }
    else {
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

  onProductCreated(product: Product) {
    // TODO: gọi service save, tạm thời push vào list
    this.products = [...this.products, { ...product, productId: Date.now() } as any];
    this.showProductForm = false;    
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
    // const summary = this.products.find(p => p.productId === productId);
    // this.selectedProduct = summary as unknown as Product;
    // this.showProductForm = true;
  }

  onDelete(product: ProductSummary) {
    const summary = this.products.find(p => p.productId === productId);
    this.selectedProduct = summary as unknown as Product;
    this.showProductForm = true;
  }

  onDelete(productId: number) {
    // this.productService.delete(id).subscribe({
    //   next: () => {
    //     this.products = this.products.filter(p => p.id !== id);
    //   },
    //   error: (err) => console.error(err)
    // });
    const product = this.products.find(p => p.productId === productId);
    this.dialogService.open({
      title: 'Confirm Delete',
      message: `Are you sure you want to delete "${product?.title}"?`,
      onConfirm: () => {
        this.productService.delete(product.productId).subscribe({
          next: () => {
            this.products = this.products.filter(p => p.productId !== p.productId);
            this.toastService.show(
              'Delete product successfully'
            );
          },

          error: (err) => {
            console.error(err);
            this.toastService.show('Failed to delete product. Please try again later.');
          }
        });
        this.products = this.products.filter(p => p.productId !== productId);
        this.toastService.show('Delete product successfully');
      }
    });
  }

  deleteSelected() {
    const count = this.selectedIds.size;
    // const ids = Array.from(this.selectedIds);
    // this.productService.deleteMany(ids).subscribe({
    //   next: () => {
    //     this.products = this.products.filter(p => !this.selectedIds.has(p.id));
    //     this.selectedIds = new Set();
    //     this.selectionMode = false;
    //   },
    //   error: (err) => console.error(err)
    // });
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
        this.products = this.products.filter(p => !this.selectedIds.has(p.productId));
        this.selectedIds = new Set();
        this.selectionMode = false;
        this.toastService.show(`Deleted ${count} products`);
      }
    });
  }

  onViewDetail(productId: number): void {
    const s = this.products.find(p => p.productId === productId);
    if (!s) return;
    this.viewDetailProduct = this.buildMockFullProduct(s);
  }

  closeViewDetail(): void { this.viewDetailProduct = null; }

  private buildMockFullProduct(s: ProductSummary): any {
    const base = {
      productId: s.productId, productType: s.productType, title: s.title,
      category: String(s.productType), barcode: '893000000' + String(s.productId).padStart(4, '0'),
      image: s.image ?? '', originalValue: Math.round(s.sellingPrice * 0.75),
      imageUrl: s.imageUrl ?? '', originalValue: Math.round(s.sellingPrice * 0.75),
      sellingPrice: s.sellingPrice, weight: 0.3, dimensions: '20 × 15 × 2 cm',
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
}