import { Injectable } from '@angular/core';
import { ProductType } from '../../models/product-type.enum';
import { Product, CdProduct } from '../../models/product.model';
import { ProductTypeDetailBuilder, ProductDetailViewModel } from './product-type-detail-builder.interface';

@Injectable({ providedIn: 'root' })
export class CdDetailBuilder implements ProductTypeDetailBuilder {

  readonly supportedType = ProductType.CD;

  build(product: Product): Pick<ProductDetailViewModel, 'detailRows' | 'tracklist'> {
    const d = (product as CdProduct).typeDetails;
    return {
      detailRows: [
        { label: 'Artist',       value: d.artists.join(', ') },
        { label: 'Record Label', value: d.recordLabel },
        { label: 'Genre',        value: d.genre },
        ...(d.releaseDate ? [{ label: 'Release Date', value: d.releaseDate }] : []),
      ],
      tracklist: d.tracks,
    };
  }
}