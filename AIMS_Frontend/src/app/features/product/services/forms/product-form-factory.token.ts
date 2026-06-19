import { InjectionToken } from '@angular/core';
import { ProductTypeFormFactory } from './product-form-factory.interface';

export const PRODUCT_FORM_FACTORIES =
  new InjectionToken<ProductTypeFormFactory[]>(
    'PRODUCT_FORM_FACTORIES'
  );