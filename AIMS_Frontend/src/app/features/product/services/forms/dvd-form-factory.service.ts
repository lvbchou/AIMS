import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductTypeFormFactory } from './product-form-factory.interface';
import { DvdProduct } from '../../models/product.model';
import { Product } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';

@Injectable()
export class DvdFormFactory implements ProductTypeFormFactory {

  readonly type = ProductType.DVD;

  buildCategoryGroup(fb: FormBuilder): FormGroup {
    return fb.group({
      discType:    ['', Validators.required],
      director:    ['', Validators.required],
      runtime:     [null, Validators.required],
      studio:      ['', Validators.required],
      language:    ['', Validators.required],
      subtitles:   ['', Validators.required],
      genre:       [''],
      releaseDate: [''],
    });
  }

  patchCategoryDetails(group: FormGroup, product: Product): void {
    if (product.productType !== ProductType.DVD) return;
    const d = (product as DvdProduct).typeDetails;
    group.patchValue({
      discType:    d.discType,
      director:    d.director,
      runtime:     d.runtime,
      studio:      d.studio,
      language:    d.language,
      subtitles:   d.subtitles,
      genre:       d.genre       ?? '',
      releaseDate: d.releaseDate ?? '',
    });
  }
}