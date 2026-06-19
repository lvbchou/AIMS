import { Injectable } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ProductTypeFormFactory } from './product-form-factory.interface';
import { CdProduct, Product } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';

@Injectable()
export class CdFormFactory implements ProductTypeFormFactory {

  readonly type = ProductType.CD;

  buildCategoryGroup(fb: FormBuilder): FormGroup {
    return fb.group({
      artists:     fb.array([new FormControl('', Validators.required)]),
      recordLabel: ['', Validators.required],
      genre:       ['', Validators.required],
      releaseDate: [''],
      tracks:      fb.array([]),
    });
  }

  patchCategoryDetails(group: FormGroup, product: Product): void {
    if (product.productType !== ProductType.CD) return;
    const fb = new FormBuilder();
    const d  = (product as CdProduct).typeDetails;

    // Patch artists FormArray
    const artistsArray = group.get('artists') as import('@angular/forms').FormArray;
    artistsArray.clear();
    (d.artists ?? ['']).forEach(a =>
      artistsArray.push(new FormControl(a, Validators.required))
    );

    // Patch tracks FormArray
    const tracksArray = group.get('tracks') as import('@angular/forms').FormArray;
    tracksArray.clear();
    (d.tracks ?? []).forEach(t =>
      tracksArray.push(fb.group({
        title:  [t.title,  Validators.required],
        length: [t.length, Validators.required],
      }))
    );

    group.patchValue({
      recordLabel: d.recordLabel,
      genre:       d.genre,
      releaseDate: d.releaseDate ?? '',
    });
  }
}