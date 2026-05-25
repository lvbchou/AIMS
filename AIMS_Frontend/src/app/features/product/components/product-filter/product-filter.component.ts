import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface PriceRange { label: string; min: number; max: number; }

export const PRICE_RANGES: PriceRange[] = [
  { label: 'Under 100,000 VND',     min: 0,      max: 99999     },
  { label: '100,000 - 200,000 VND', min: 100000, max: 200000    },
  { label: '200,000 - 300,000 VND', min: 200000, max: 300000    },
  { label: 'Over 300,000 VND',      min: 300001, max: 999999999 },
];

@Component({
  selector: 'app-product-filter',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-filter.component.html',
  styleUrls: ['./product-filter.component.scss']
})
export class ProductFilterComponent {
  @Output() filterApplied = new EventEmitter<PriceRange | null>();

  ranges = PRICE_RANGES;
  selected: PriceRange | null = null;

  /**
   * Checkbox toggle: chọn range → selected = range
   * Uncheck cùng range → selected = null (bỏ filter)
   * Đúng với Activity Diagram: user "select price range" → 1 lựa chọn tại 1 thời điểm
   */
  select(r: PriceRange): void {
    this.selected = this.selected === r ? null : r;
  }

  isSelected(r: PriceRange): boolean {
    return this.selected === r;
  }

  /** SD step 2: emit range đã chọn (hoặc null nếu đã bỏ chọn tất cả) */
  apply(): void {
    this.filterApplied.emit(this.selected);
  }

  /** Xóa filter, reset về search result ban đầu */
  clear(): void {
    this.selected = null;
    this.filterApplied.emit(null);
  }
}