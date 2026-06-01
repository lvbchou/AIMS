import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StockItem } from '../../models/stock.model';

@Component({
  selector: 'app-stock-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stock-table.component.html',
  styleUrls: ['./stock-table.component.scss'],
})
export class StockTableComponent {
  @Input()  stocks: StockItem[] = [];
  @Output() edit    = new EventEmitter<StockItem>();
  @Output() history = new EventEmitter<StockItem>();

  onEdit(item: StockItem) {
    this.edit.emit(item);
  }

  onHistory(item: StockItem) {
    this.history.emit(item);
  }
}