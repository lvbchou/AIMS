import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder, FormGroup, Validators, ReactiveFormsModule
} from '@angular/forms';
import { StockItem, StockAdjustment, AdjustType } from '../../models/stock.model';

@Component({
  selector: 'app-stock-adjust-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './stock-adjust-form.component.html',
  styleUrls: ['./stock-adjust-form.component.scss'],
})
export class StockAdjustFormComponent implements OnInit {
  @Input()  item!: StockItem;
  @Output() submitted = new EventEmitter<StockAdjustment>();
  @Output() cancelled = new EventEmitter<void>();

  form!: FormGroup;

  readonly adjustTypes: { value: AdjustType; label: string }[] = [
    { value: 'IMPORT',     label: 'Import — Nhập hàng'         },
    { value: 'EXPORT',     label: 'Export — Xuất hàng'         },
    { value: 'DAMAGE',     label: 'Damage — Hàng hỏng'         },
    { value: 'CORRECTION', label: 'Correction — Điều chỉnh'    },
  ];

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      adjustType: ['IMPORT', Validators.required],
      quantity:   [1, [Validators.required, Validators.min(1)]],
      reason:     ['', Validators.required],
    });
    document.body.style.overflow = 'hidden';
  }

  // Preview quantity sau khi điều chỉnh
  get previewQuantity(): number {
    const type: AdjustType = this.form.get('adjustType')?.value;
    const qty: number       = this.form.get('quantity')?.value ?? 0;
    const isIncrease        = type === 'IMPORT' || type === 'CORRECTION';
    const result            = isIncrease
      ? this.item.quantity + qty
      : this.item.quantity - qty;
    return Math.max(0, result);
  }

  get isDecrease(): boolean {
    const type: AdjustType = this.form.get('adjustType')?.value;
    return type === 'EXPORT' || type === 'DAMAGE';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { adjustType, quantity, reason } = this.form.value;
    const isIncrease = adjustType === 'IMPORT' || adjustType === 'CORRECTION';

    const adjustment: StockAdjustment = {
      id:             Date.now(),
      productId:      this.item.productId,
      adjustType,
      quantity,
      reason,
      adjustedAt:     new Date(),
      quantityBefore: this.item.quantity,
      quantityAfter:  this.previewQuantity,
    };

    document.body.style.overflow = '';
    this.submitted.emit(adjustment);
  }

  onCancel(): void {
    document.body.style.overflow = '';
    this.cancelled.emit();
  }
}