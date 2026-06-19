import { Injectable } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ProductTypeFormFactory } from './product-form-factory.interface';
import { NewspaperProduct, Product } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';

@Injectable()
export class NewspaperFormFactory implements ProductTypeFormFactory {

  readonly type = ProductType.NEWSPAPER;

  buildCategoryGroup(fb: FormBuilder): FormGroup {
    return fb.group({
      editorInChief:        ['', Validators.required],
      issueNumber:          [''],
      publicationFrequency: [''],
      issn:                 [''],
      publisher:            ['', Validators.required],
      publicationDate:      ['', Validators.required],
      language:             [''],
      sections:             fb.array([new FormControl('')]),
    });
  }

  patchCategoryDetails(group: FormGroup, product: Product): void {
    if (product.productType !== ProductType.NEWSPAPER) return;
    const d = (product as NewspaperProduct).typeDetails;

    // Patch sections FormArray
    const sectionsArray = group.get('sections') as import('@angular/forms').FormArray;
    sectionsArray.clear();
    (d.sections ?? ['']).forEach(s =>
      sectionsArray.push(new FormControl(s))
    );

    group.patchValue({
      editorInChief:        d.editorInChief,
      issueNumber:          d.issueNumber          ?? '',
      publicationFrequency: d.publicationFrequency ?? '',
      issn:                 d.issn                 ?? '',
      publisher:            d.publisher,
      publicationDate:      d.publicationDate,
      language:             d.language             ?? '',
    });
  }
}