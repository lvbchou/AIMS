import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormArray,
  FormControl,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { CommonModule } from '@angular/common';

import { ProductType } from '../../models/product-type.enum';
import { Product } from '../../models/product.model';
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
export class ProductFormComponent implements OnInit {
  @Output() submitted = new EventEmitter<Product>();
  @Output() cancelled = new EventEmitter<void>();

  // Nếu có product → update mode, không có → create mode
  @Input() product: Product | null = null;

  readonly ProductType = ProductType;
  readonly types = Object.values(ProductType);

  form!: FormGroup;

  // Tiêu đề động theo mode
  get isUpdateMode(): boolean {
    return this.product !== null;
  }

  get modalTitle(): string {
    return this.isUpdateMode ? 'Update Product' : 'Add New Product';
  }

  get submitLabel(): string {
    return this.isUpdateMode ? 'Update' : 'Add Product';
  }

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    const initialType = (this.product?.productType as ProductType) ?? ProductType.DVD;
    this.buildForm(initialType);

    // Nếu là update mode → patch data vào form
    if (this.product) {
      this.patchForm(this.product);
    }

    document.body.style.overflow = 'hidden';
  }

  // ── Getters ────────────────────────────────────────────────────────────────
  get selectedType(): ProductType {
    return this.form.get('productType')?.value;
  }

  get generalForm(): FormGroup {
    return this.form.get('general') as FormGroup;
  }

  get categoryForm(): FormGroup {
    return this.form.get('categoryDetails') as FormGroup;
  }

  // ── Form builder ───────────────────────────────────────────────────────────
  private buildForm(type: ProductType): void {
    this.form = this.fb.group({
      productType: [type, Validators.required],
      general: this.fb.group({
        title:         ['', Validators.required],
        category:      ['', Validators.required],
        barcode:       ['', Validators.required],
        image:      ['', Validators.required],
        imageUrl:      ['', Validators.required],
        originalValue: [0, [Validators.required, Validators.min(0)]],
        sellingPrice:  [0, [Validators.required, Validators.min(0)]],
        weight:        [0, [Validators.required, Validators.min(0)]],
        dimensions:    ['', Validators.required],
        description:   ['', Validators.required],
      }),
      categoryDetails: this.buildCategoryGroup(type),
    });
  }

  // ── Patch data vào form khi update ────────────────────────────────────────
  private patchForm(product: Product): void {
    this.form.patchValue({ productType: product.productType });

    // Patch general — map image → imageUrl
    this.generalForm.patchValue({
      title:         product.title,
      category:      product.category,
      barcode:       product.barcode,
      image:      product.image,      // ← backend trả về 'image', form dùng 'imageUrl'
      originalValue: product.originalValue,
      sellingPrice:  product.sellingPrice,
      weight:        product.weight,
      dimensions:    product.dimensions,
      description:   product.description,
    });
    // Truyền thẳng product (flat) vào patchCategoryDetails
    this.patchCategoryDetails(product.productType, product);
    const { productType, categoryDetails, ...general } = product as any;

    this.form.patchValue({
      productType,
      general,
    });

    // Patch categoryDetails theo từng type
    this.patchCategoryDetails(productType, categoryDetails);
  }

  private patchCategoryDetails(type: ProductType, details: any): void {
    if (!details) return;

    const group = this.form.get('categoryDetails') as FormGroup;

    switch (type) {
      case ProductType.CD: {
        // Patch artists FormArray
        const artistsArray = group.get('artists') as FormArray;
        artistsArray.clear();
        (details.artists ?? ['']).forEach((a: string) => {
          artistsArray.push(new FormControl(a, Validators.required));
        });

        // Patch tracklist FormArray
        const tracklistArray = group.get('tracklist') as FormArray;
        tracklistArray.clear();
        (details.tracks ?? []).forEach((t: any) => {
          tracklistArray.push(this.fb.group({
            title:  [t.trackTitle  ?? '', Validators.required],
            length: [t.trackLength ?? '', Validators.required],
        (details.tracklist ?? []).forEach((t: any) => {
          tracklistArray.push(this.fb.group({
            title:  [t.title  ?? '', Validators.required],
            length: [t.length ?? '', Validators.required],
          }));
        });

        group.patchValue({
          recordLabel: details.recordLabel ?? '',
          genre:       details.genre       ?? '',
          releaseDate: details.releaseDate  ?? '',
        });
        break;
      }

      case ProductType.NEWSPAPER: {
        // Patch sections FormArray
        const sectionsArray = group.get('sections') as FormArray;
        sectionsArray.clear();
        (details.sections ?? ['']).forEach((s: string) => {
          sectionsArray.push(new FormControl(s));
        });

        group.patchValue({
          editorInChief:        details.editorInChief        ?? '',
          issueNumber:          details.issueNumber          ?? '',
          publicationFrequency: details.publicationFrequency ?? '',
          ISSN:                 details.ISSN                 ?? '',
          isbn:                 details.isbn                 ?? '',
          publisher:            details.publisher            ?? '',
          publicationDate:      details.publicationDate      ?? '',
          language:             details.language             ?? '',
        });
        break;
      }
      case ProductType.BOOK:
        group.patchValue({
          author:          details.author          ?? '',
          coverType:       details.coverType       ?? '',
          pages:           details.pages           ?? null,
          genre:           details.genre           ?? '',
          publisher:       details.publisher       ?? '',
          publicationDate: details.publicationDate ?? '',
          language:        details.language        ?? '',
        });
        break;
      case ProductType.DVD:
        group.patchValue({
          discType:    details.discType    ?? '',
          director:    details.director    ?? '',
          runtime:     details.runtime     ?? null,
          studio:      details.studio      ?? '',
          language:    details.language    ?? '',
          subtitles:   details.subtitles   ?? '',
          genre:       details.genre       ?? '',
          releaseDate: details.releaseDate ?? '',
        });

      default:
        // DVD và Book không có FormArray → patchValue thẳng
        group.patchValue(details);
        break;
    }
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
          tracklist:   this.fb.array([]),
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
          ISSN:                 [''],
          isbn:                 [''],
          publisher:            ['', Validators.required],
          publicationDate:      ['', Validators.required],
          language:             [''],
          sections:             this.fb.array([new FormControl('')]),
        });
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
    const payload: Product = {
      productType,
      ...general,
      ...categoryDetails,
      // Giữ lại productId nếu là update mode
      ...(this.product?.productId ? { productId: this.product.productId } : {}),
    }

    // document.body.style.overflow = '';
    this.submitted.emit(payload);
    const product = {
      ...general,
      productType,
      categoryDetails,
      // Giữ lại productId nếu là update mode
      ...(this.product?.productId ? { productId: this.product.productId } : {}),
    } as Product;

    document.body.style.overflow = '';
    this.submitted.emit(product);
  }

  onCancel(): void {
    document.body.style.overflow = '';
    this.cancelled.emit();
  }
}