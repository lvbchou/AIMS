import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductTypeFormFactory } from './product-form-factory.interface';
import { BookProduct, Product } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';

@Injectable()
export class BookFormFactory implements ProductTypeFormFactory {

  readonly type = ProductType.BOOK;

  buildCategoryGroup(fb: FormBuilder): FormGroup {
    return fb.group({
      author:          ['', Validators.required],
      coverType:       ['', Validators.required],
      pages:           [null],
      genre:           [''],
      publisher:       ['', Validators.required],
      publicationDate: ['', Validators.required],
      language:        [''],
    });
  }

  patchCategoryDetails(group: FormGroup, product: Product): void {
    if (product.productType !== ProductType.BOOK) return;
    const d = (product as BookProduct).typeDetails;
    group.patchValue({
      author:          d.author,
      coverType:       d.coverType,
      pages:           d.pages           ?? null,
      genre:           d.genre           ?? '',
      publisher:       d.publisher,
      publicationDate: d.publicationDate,
      language:        d.language        ?? '',
    });
  }
}