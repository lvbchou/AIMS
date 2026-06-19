import { Injectable } from '@angular/core';
import { ProductType } from '../models/product-type.enum';

export interface ProductTypeDisplay {
  label: string;
  cssClass: string;
}

@Injectable({ providedIn: 'root' })
export class ProductTypeDisplayService {

  private readonly displayMap: ReadonlyMap<ProductType, ProductTypeDisplay> = new Map([
    [ProductType.BOOK,      { label: 'BOOK',      cssClass: 'badge-book'      }],
    [ProductType.CD,        { label: 'CD',        cssClass: 'badge-cd'        }],
    [ProductType.DVD,       { label: 'DVD',       cssClass: 'badge-dvd'       }],
    [ProductType.NEWSPAPER, { label: 'NEWSPAPER', cssClass: 'badge-newspaper' }],
  ]);

  get(type: ProductType): ProductTypeDisplay {
    return this.displayMap.get(type) ?? { label: String(type), cssClass: '' };
  }

  label(type: ProductType): string    { return this.get(type).label; }
  cssClass(type: ProductType): string { return this.get(type).cssClass; }
}