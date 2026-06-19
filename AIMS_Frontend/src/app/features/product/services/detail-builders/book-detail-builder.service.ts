import { Injectable } from '@angular/core';
import { ProductType } from '../../models/product-type.enum';
import { Product, BookProduct } from '../../models/product.model';
import { ProductTypeDetailBuilder, ProductDetailViewModel } from './product-type-detail-builder.interface';

@Injectable({ providedIn: 'root' })
export class BookDetailBuilder implements ProductTypeDetailBuilder {

  readonly supportedType = ProductType.BOOK;

  build(product: Product): Pick<ProductDetailViewModel, 'detailRows'> {
    const d = (product as BookProduct).typeDetails;
    return {
      detailRows: [
        { label: 'Author',            value: d.author },
        { label: 'Cover Type',        value: d.coverType },
        { label: 'Publisher',         value: d.publisher },
        { label: 'Publication Date',  value: d.publicationDate },
        ...(d.pages    ? [{ label: 'Pages',    value: `${d.pages} pages` }] : []),
        ...(d.genre    ? [{ label: 'Genre',    value: d.genre }]            : []),
        ...(d.language ? [{ label: 'Language', value: d.language }]         : []),
      ],
    };
  }
}