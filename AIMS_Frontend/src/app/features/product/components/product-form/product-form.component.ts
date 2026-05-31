import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import {
  FormBuilder, FormGroup, FormArray,
  FormControl, Validators, ReactiveFormsModule,
} from '@angular/forms';
import { CommonModule } from '@angular/common';

import { ProductType } from '../../models/product-type.enum';
import {
  Product, DvdProduct, CdProduct, BookProduct, NewspaperProduct
} from '../../models/product.model';
import { DvdFormComponent } from '../forms/dvd-form/dvd-form.component';
import { CdFormComponent } from '../forms/cd-form/cd-form.component';
import { BookFormComponent } from '../forms/book-form/book-form.component';
import { NewspaperFormComponent } from '../forms/newspaper-form/newspaper-form.component';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    DvdFormComponent, CdFormComponent,
    BookFormComponent, NewspaperFormComponent,
  ],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss'],
})
export class ProductFormComponent implements OnInit, OnDestroy {
  @Output() submitted = new EventEmitter<Product>();
  @Output() cancelled = new EventEmitter<void>();
  @Input()  product: Product | null = null;

  readonly ProductType = ProductType;
  readonly types = Object.values(ProductType);

  form!: FormGroup;

  get isUpdateMode(): boolean { return this.product !== null; }
  get modalTitle():   string  { return this.isUpdateMode ? 'Update Product' : 'Add New Product'; }
  get submitLabel():  string  { return this.isUpdateMode ? 'Update' : 'Add Product'; }
  get selectedType(): ProductType { return this.form.get('productType')?.value; }
  get generalForm():  FormGroup { return this.form.get('general') as FormGroup; }
  get categoryForm(): FormGroup { return this.form.get('categoryDetails') as FormGroup; }

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    const initialType = this.product?.productType ?? ProductType.DVD;
    this.buildForm(initialType);
    if (this.product) this.patchForm(this.product);
    document.body.style.overflow = 'hidden';
  }

  ngOnDestroy(): void {
    document.body.style.overflow = '';
  }

  // ── Form builder ───────────────────────────────────────────────────────────
  private buildForm(type: ProductType): void {
    this.form = this.fb.group({
      productType: [type, Validators.required],
      general: this.fb.group({
        title:         ['', Validators.required],
        category:      ['', Validators.required],
        barcode:       ['', Validators.required],
        image:         ['', Validators.required],
        originalValue: [0, [Validators.required, Validators.min(0)]],
        sellingPrice:  [0, [Validators.required, Validators.min(0)]],
        weight:        [0, [Validators.required, Validators.min(0)]],
        dimensions:    ['', Validators.required],
        description:   ['', Validators.required],
      }),
      categoryDetails: this.buildCategoryGroup(type),
    });
  }

  private buildCategoryGroup(type: ProductType): FormGroup {
    switch (type) {
      case ProductType.DVD:
        return this.fb.group({
          discType:    ['', Validators.required],
          director:    ['', Validators.required],
          runtime:     [null, Validators.required],
          studio:      ['', Validators.required],
          language:    ['', Validators.required],
          subtitles:   ['', Validators.required],
          genre:       [''],
          releaseDate: [''],
        });

      case ProductType.CD:
        return this.fb.group({
          artists:     this.fb.array([new FormControl('', Validators.required)]),
          recordLabel: ['', Validators.required],
          genre:       ['', Validators.required],
          releaseDate: [''],
          tracks:      this.fb.array([]),
        });

      case ProductType.BOOK:
        return this.fb.group({
          author:          ['', Validators.required],
          coverType:       ['', Validators.required],
          pages:           [null],
          genre:           [''],
          publisher:       ['', Validators.required],
          publicationDate: ['', Validators.required],
          language:        [''],
        });

      case ProductType.NEWSPAPER:
        return this.fb.group({
          editorInChief:        ['', Validators.required],
          issueNumber:          [''],
          publicationFrequency: [''],
          issn:                 [''],
          publisher:            ['', Validators.required],
          publicationDate:      ['', Validators.required],
          language:             [''],
          sections:             this.fb.array([new FormControl('')]),
        });
    }
  }

  // ── Patch form khi update ─────────────────────────────────────────────────
  private patchForm(product: Product): void {
    this.form.patchValue({ productType: product.productType });

    this.generalForm.patchValue({
      title:         product.title,
      category:      product.category,
      barcode:       product.barcode,
      image:         product.image,
      originalValue: product.originalValue,
      sellingPrice:  product.sellingPrice,
      weight:        product.weight,
      dimensions:    product.dimensions,
      description:   product.description,
    });

    this.patchCategoryDetails(product);
  }

  private patchCategoryDetails(product: Product): void {
    const group = this.categoryForm;

    switch (product.productType) {

      case ProductType.DVD: {
        const d = (product as DvdProduct).typeDetails;
        group.patchValue({
          discType: d.discType,
          director: d.director,
          runtime: d.runtime,
          studio: d.studio,
          language: d.language,
          subtitles: d.subtitles,
          genre: d.genre ?? '',
          releaseDate: d.releaseDate ?? '',
        });
        break;
      }

      case ProductType.CD: {
        const d = (product as CdProduct).typeDetails;

        const artistsArray = group.get('artists') as FormArray;
        artistsArray.clear();
        (d.artists ?? ['']).forEach(a =>
          artistsArray.push(new FormControl(a, Validators.required))
        );

        const tracksArray = group.get('tracks') as FormArray;
        tracksArray.clear();
        (d.tracks ?? []).forEach(t =>
          tracksArray.push(this.fb.group({
            title:  [t.title,  Validators.required],
            length: [t.length, Validators.required],
          }))
        );

        group.patchValue({
          recordLabel: d.recordLabel,
          genre:       d.genre,
          releaseDate: d.releaseDate ?? '',
        });
        break;
      }

      case ProductType.BOOK: {
        const d = (product as BookProduct).typeDetails;
        group.patchValue({
          author: d.author,
          coverType: d.coverType,
          pages: d.pages ?? null,
          genre: d.genre ?? '',
          publisher: d.publisher,
          publicationDate: d.publicationDate,
          language: d.language ?? '',
        });
        break;
      }

      case ProductType.NEWSPAPER: {
        const d = (product as NewspaperProduct).typeDetails;

        const sectionsArray = group.get('sections') as FormArray;
        sectionsArray.clear();
        (d.sections ?? ['']).forEach(s =>
          sectionsArray.push(new FormControl(s))
        );

        group.patchValue({
          editorInChief:        d.editorInChief,
          issueNumber:          d.issueNumber          ?? '',
          publicationFrequency: d.publicationFrequency ?? '',
          issn:                 d.issn                 ?? '',
          publisher:            d.publisher,
          publicationDate:      d.publicationDate,
          language:             d.language             ?? '',
        });
        break;
      }
    }
  }

  // ── Type change ────────────────────────────────────────────────────────────
  onTypeChange(type: ProductType): void {
    this.form.setControl('categoryDetails', this.buildCategoryGroup(type));
    this.form.patchValue({ productType: type });
  }

  // ── Submit ─────────────────────────────────────────────────────────────────
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
