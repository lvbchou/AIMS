import { Inject, Injectable } from '@angular/core';
import { ProductType } from '../../models/product-type.enum';
import { Product } from '../../models/product.model';
import { ProductTypeDisplayService } from '../product-type-display.service';
import { ProductDetailViewModel } from '../detail-builders/product-type-detail-builder.interface';
import { ProductTypeDetailBuilder } from '../detail-builders/product-type-detail-builder.interface';
import { PRODUCT_DETAIL_BUILDERS } from '../detail-builders/product-detail-builder.token';

@Injectable({ providedIn: 'root' })
export class ProductDetailBuilderService {

  private readonly registry =
    new Map<ProductType, ProductTypeDetailBuilder>();

  constructor(
    private typeDisplay: ProductTypeDisplayService,

    @Inject(PRODUCT_DETAIL_BUILDERS)
    builders: ProductTypeDetailBuilder[]
  ) {
    builders.forEach(builder => {
      this.registry.set(
        builder.supportedType,
        builder
      );
    });
  }

  build(product: Product): ProductDetailViewModel {

    const builder =
      this.registry.get(product.productType);

    if (!builder) {
      throw new Error(
        `No detail builder registered for ${product.productType}`
      );
    }

    const result = builder.build(product);

    return {
      typeLabel:
        this.typeDisplay.label(product.productType),

      typeClass:
        this.typeDisplay.cssClass(product.productType),

      detailRows:
        result.detailRows,

      tracklist:
        result.tracklist,
    };
  }
}