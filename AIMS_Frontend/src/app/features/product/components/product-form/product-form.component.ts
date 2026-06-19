import {
  Component, EventEmitter, Input,
  OnDestroy, OnInit, Output,
} from '@angular/core';
import { ReactiveFormsModule, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { ProductType } from '../../models/product-type.enum';
import { Product } from '../../models/product.model';
import { ProductFormService } from '../../services/forms/product-form.service';

// Sub-form components (ISP: each only receives its own FormGroup slice)
import { DvdFormComponent } from '../forms/dvd-form/dvd-form.component';
import { CdFormComponent } from '../forms/cd-form/cd-form.component';
import { BookFormComponent } from '../forms/book-form/book-form.component';
import { NewspaperFormComponent } from '../forms/newspaper-form/newspaper-form.component';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DvdFormComponent,
    CdFormComponent,
    BookFormComponent,
    NewspaperFormComponent,
  ],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss'],
})
export class ProductFormComponent implements OnInit, OnDestroy {
  @Input()  product: Product | null = null;
  @Output() submitted  = new EventEmitter<Product>();
  @Output() cancelled  = new EventEmitter<void>();

  readonly ProductType = ProductType;
  readonly types = Object.values(ProductType);

  form!: FormGroup;

  // ── Derived state ──────────────────────────────────────────────────────────
  get isUpdateMode(): boolean  { return this.product !== null; }
  get modalTitle():   string   { return this.isUpdateMode ? 'Update Product' : 'Add New Product'; }
  get submitLabel():  string   { return this.isUpdateMode ? 'Update' : 'Add Product'; }
  get selectedType(): ProductType { return this.form.get('productType')?.value; }
  get generalForm():  FormGroup   { return this.form.get('general')         as FormGroup; }
  get categoryForm(): FormGroup   { return this.form.get('categoryDetails') as FormGroup; }

  constructor(private formService: ProductFormService) {}

  // ── Lifecycle ──────────────────────────────────────────────────────────────
  ngOnInit(): void {
    const initialType = this.product?.productType ?? ProductType.DVD;

    // SRP: delegate form construction entirely to the service
    this.form = this.formService.buildForm(initialType);

    if (this.product) {
      this.formService.patchForm(this.form, this.product);
    }

    document.body.style.overflow = 'hidden';
  }

  ngOnDestroy(): void {
    document.body.style.overflow = '';
  }

  // ── Event handlers ─────────────────────────────────────────────────────────

  /** Swap the categoryDetails sub-group when the user picks a different type. */
  onTypeChange(type: ProductType): void {
    // OCP: no switch/case — the service looks up the correct factory
    this.form.setControl('categoryDetails', this.formService.buildCategoryGroup(type));
    this.form.patchValue({ productType: type });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { productType, general, categoryDetails } = this.form.value;

    const product = {
      ...general,
      productType,
      typeDetails: categoryDetails,
      ...(this.product?.productId ? { productId: this.product.productId } : {}),
      ...(this.product?.status    ? { status:    this.product.status }    : {}),
    } as Product;

    this.submitted.emit(product);
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}