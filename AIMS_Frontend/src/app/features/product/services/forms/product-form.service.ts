import { Inject, Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductType } from '../../models/product-type.enum';
import { Product } from '../../models/product.model';
import { ProductTypeFormFactory } from './product-form-factory.interface';
import { PRODUCT_FORM_FACTORIES } from './product-form-factory.token';

@Injectable({ providedIn: 'root' })
export class ProductFormService {

  /** Registry: ProductType → factory. OCP extension point. */
  private readonly factoryMap: ReadonlyMap<ProductType, ProductTypeFormFactory>;

  constructor(
    private fb: FormBuilder,

    @Inject(PRODUCT_FORM_FACTORIES)
    factories: ProductTypeFormFactory[]
  ) {

    this.factoryMap = new Map(
      factories.map(f => [f.type, f])
    );
  }

  buildForm(type: ProductType): FormGroup {
    return this.fb.group({
      productType: [type, Validators.required],
      general: this.buildGeneralGroup(),
      categoryDetails: this.buildCategoryGroup(type),
    });
  }

  buildCategoryGroup(type: ProductType): FormGroup {
    return this.getFactory(type).buildCategoryGroup(this.fb);
  }

  patchForm(form: FormGroup, product: Product): void {
    form.patchValue({ productType: product.productType });

    (form.get('general') as FormGroup).patchValue({
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

    this.getFactory(product.productType)
        .patchCategoryDetails(form.get('categoryDetails') as FormGroup, product);
  }

  // ── Private helpers ────────────────────────────────────────────────────────

  private buildGeneralGroup(): FormGroup {
    return this.fb.group({
      title:         ['', Validators.required],
      category:      ['', Validators.required],
      barcode:       ['', Validators.required],
      image:         ['', Validators.required],
      originalValue: [0, [Validators.required, Validators.min(0)]],
      sellingPrice:  [0, [Validators.required, Validators.min(0)]],
      weight:        [0, [Validators.required, Validators.min(0)]],
      dimensions:    ['', Validators.required],
      description:   ['', Validators.required],
    });
  }

  private getFactory(type: ProductType): ProductTypeFormFactory {
    const factory = this.factoryMap.get(type);
    if (!factory) {
      throw new Error(`[ProductFormService] No factory registered for ProductType "${type}".`);
    }
    return factory;
  }
}