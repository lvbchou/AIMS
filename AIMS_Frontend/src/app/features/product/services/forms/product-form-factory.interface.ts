import { FormBuilder, FormGroup } from '@angular/forms';
import { Product } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';

export interface ProductTypeFormFactory {

  readonly type: ProductType;

  buildCategoryGroup(fb: FormBuilder): FormGroup;
  patchCategoryDetails(group: FormGroup, product: Product): void;
}