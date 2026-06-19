import { Product } from '../../models/product.model';
import { ProductType } from '../../models/product-type.enum';

export interface ProductDetailRow {
  label: string;
  value: string;
}

export interface ProductDetailViewModel {
  typeLabel:  string;
  typeClass:  string;
  detailRows: ProductDetailRow[];
  tracklist?: { title: string; length: string }[];
}

/**
 * ProductTypeDetailBuilder — abstraction mỗi type implement.
 *
 * OCP: thêm type mới = tạo class mới implement interface này.
 * Không sửa bất kỳ file nào đã có.
 */
export interface ProductTypeDetailBuilder {
  readonly supportedType: ProductType;
  build(product: Product): Pick<ProductDetailViewModel, 'detailRows' | 'tracklist'>;
}