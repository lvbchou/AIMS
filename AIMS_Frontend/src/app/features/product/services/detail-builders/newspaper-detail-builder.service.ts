import { Injectable } from '@angular/core';
import { ProductType } from '../../models/product-type.enum';
import { Product, NewspaperProduct } from '../../models/product.model';
import { ProductTypeDetailBuilder, ProductDetailViewModel } from './product-type-detail-builder.interface';

@Injectable({ providedIn: 'root' })
export class NewspaperDetailBuilder implements ProductTypeDetailBuilder {

  readonly supportedType = ProductType.NEWSPAPER;

  build(product: Product): Pick<ProductDetailViewModel, 'detailRows'> {
    const d = (product as NewspaperProduct).typeDetails;
    return {
      detailRows: [
        { label: 'Editor in Chief',   value: d.editorInChief },
        { label: 'Publisher',         value: d.publisher },
        { label: 'Publication Date',  value: d.publicationDate },
        ...(d.issueNumber          ? [{ label: 'Issue Number',          value: d.issueNumber }]          : []),
        ...(d.publicationFrequency ? [{ label: 'Publication Frequency', value: d.publicationFrequency }] : []),
        ...(d.issn                 ? [{ label: 'ISSN',                  value: d.issn }]                 : []),
        ...(d.language             ? [{ label: 'Language',              value: d.language }]             : []),
        ...(d.sections?.length     ? [{ label: 'Sections',              value: d.sections!.join(', ') }] : []),
      ],
    };
  }
}