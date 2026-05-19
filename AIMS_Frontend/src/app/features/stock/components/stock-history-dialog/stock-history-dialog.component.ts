import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StockAdjustment, StockItem } from '../../models/stock.model';

@Component({
  selector: 'app-stock-history-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stock-history-dialog.component.html',
  styleUrls: ['./stock-history-dialog.component.scss'],
})
export class StockHistoryDialogComponent {
  @Input() item!: StockItem;
  @Input() history: StockAdjustment[] = [];
  @Output() closed = new EventEmitter<void>();

  readonly typeLabels: Record<string, string> = {
    IMPORT:     'Import',
    EXPORT:     'Export',
    DAMAGE:     'Damage',
    CORRECTION: 'Correction',
  };

  readonly typeColors: Record<string, string> = {
    IMPORT:     'increase',
    EXPORT:     'decrease',
    DAMAGE:     'decrease',
    CORRECTION: 'neutral',
  };

  onClose(): void {
    this.closed.emit();
  }
}