import { InjectionToken } from '@angular/core';
import { ProductTypeDetailBuilder } from './product-type-detail-builder.interface';

export const PRODUCT_DETAIL_BUILDERS =
  new InjectionToken<ProductTypeDetailBuilder[]>(
    'PRODUCT_DETAIL_BUILDERS'
  );