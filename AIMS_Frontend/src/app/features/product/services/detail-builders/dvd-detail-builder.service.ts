import { Injectable } from '@angular/core';
import { ProductType } from '../../models/product-type.enum';
import { Product, DvdProduct } from '../../models/product.model';
import { ProductTypeDetailBuilder, ProductDetailViewModel } from './product-type-detail-builder.interface';

@Injectable({ providedIn: 'root' })
export class DvdDetailBuilder implements ProductTypeDetailBuilder {

  readonly supportedType = ProductType.DVD;

  build(product: Product): Pick<ProductDetailViewModel, 'detailRows'> {
    const d = (product as DvdProduct).typeDetails;
    return {
      detailRows: [
        { label: 'Disc Type',  value: d.discType },
        { label: 'Director',   value: d.director },
        { label: 'Runtime',    value: `${d.runtime} min` },
        { label: 'Studio',     value: d.studio },
        { label: 'Language',   value: d.language },
        { label: 'Subtitles',  value: d.subtitles },
        ...(d.genre       ? [{ label: 'Genre',        value: d.genre }]       : []),
        ...(d.releaseDate ? [{ label: 'Release Date', value: d.releaseDate }] : []),
      ],
    };
  }
}