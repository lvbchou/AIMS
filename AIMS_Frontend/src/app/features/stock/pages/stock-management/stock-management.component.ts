import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductType } from '../../../product/models/product-type.enum';
import { StockItem, StockAdjustment } from '../../models/stock.model';
import { StockAdjustFormComponent } from '../../components/stock-adjust-form/stock-adjust-form.component';
import { StockHistoryDialogComponent } from '../../components/stock-history-dialog/stock-history-dialog.component';
import { ToastComponent } from '../../../../shared/components/toast/toast/toast.component';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { StockTableComponent } from '../../components/stock-table/stock-table.component';

@Component({
  selector: 'app-stock-management',
  standalone: true,
  imports: [
    CommonModule,
    StockAdjustFormComponent,
    StockHistoryDialogComponent,
    StockTableComponent,
    ToastComponent,
  ],
  templateUrl: './stock-management.component.html',
  styleUrls: ['./stock-management.component.scss'],
})
export class StockManagementComponent implements OnInit {

  constructor(private toastService: ToastService) {}

  stocks: StockItem[] = [];

  // Key: productId → list adjustments
  adjustmentHistory: Map<number, StockAdjustment[]> = new Map();

  // Popup state
  selectedItem: StockItem | null = null;
  showAdjustForm   = false;
  showHistoryDialog = false;

  ngOnInit(): void {
    // Mock data
    this.stocks = [
      { productId: 1,  title: 'The Last Horizon',            productType: ProductType.DVD,       quantity: 12  },
      { productId: 2,  title: 'Echoes of Silence',            productType: ProductType.CD,        quantity: 8   },
      { productId: 3,  title: 'Pop Hits 2023 - Various',      productType: ProductType.CD,        quantity: 10  },
      { productId: 4,  title: 'Jazz Classics: Midnight Soul',  productType: ProductType.CD,        quantity: 7   },
      { productId: 5,  title: 'Interstellar Special Edition',  productType: ProductType.DVD,       quantity: 3   },
      { productId: 6,  title: 'Wild Earth',                   productType: ProductType.DVD,       quantity: 0   },
      { productId: 7,  title: 'Shadow Alley',                 productType: ProductType.DVD,       quantity: 4   },
      { productId: 8,  title: 'Tuoi Tho Du Doi',              productType: ProductType.BOOK,      quantity: 22  },
      { productId: 9,  title: 'Atomic Habits',                productType: ProductType.BOOK,      quantity: 18  },
      { productId: 10, title: 'Sapiens: A Brief History',     productType: ProductType.BOOK,      quantity: 9   },
      { productId: 11, title: 'Tuoi Tre Newspaper',           productType: ProductType.NEWSPAPER, quantity: 100 },
      { productId: 12, title: 'Thanh Nien Weekly',            productType: ProductType.NEWSPAPER, quantity: 80  },
      { productId: 13, title: 'Classical Symphony Vol.1',     productType: ProductType.CD,        quantity: 6   },
      { productId: 14, title: 'Rock Anthems Greatest',        productType: ProductType.CD,        quantity: 11  },
      { productId: 15, title: 'Inception (2-Disc)',           productType: ProductType.DVD,       quantity: 5   },
      { productId: 16, title: 'The Grand Budapest Hotel',     productType: ProductType.DVD,       quantity: 7   },
      { productId: 17, title: 'So Do',                        productType: ProductType.BOOK,      quantity: 25  },
    ];
  }

  // ── Getters ───────────────────────────────────────────────────────────────
  get totalUnits(): number {
    return this.stocks.reduce((sum, s) => sum + s.quantity, 0);
  }

  getHistory(productId: number): StockAdjustment[] {
    return this.adjustmentHistory.get(productId) ?? [];
  }

  // ── Open adjust form ──────────────────────────────────────────────────────
  onEdit(item: StockItem): void {
    this.selectedItem    = item;
    this.showAdjustForm  = true;
  }

  // ── Open history dialog ───────────────────────────────────────────────────
  onHistory(item: StockItem): void {
    this.selectedItem      = item;
    this.showHistoryDialog = true;
  }

  // ── Handle adjustment submitted ───────────────────────────────────────────
  onAdjustSubmitted(adjustment: StockAdjustment): void {
    // Cập nhật quantity trong danh sách
    this.stocks = this.stocks.map(s =>
      s.productId === adjustment.productId
        ? { ...s, quantity: adjustment.quantityAfter }
        : s
    );

    // Lưu vào history
    const existing = this.adjustmentHistory.get(adjustment.productId) ?? [];
    this.adjustmentHistory.set(adjustment.productId, [adjustment, ...existing]);

    this.showAdjustForm = false;
    this.selectedItem   = null;
    this.toastService.show('Stock adjusted successfully');
  }

  onAdjustCancelled(): void {
    this.showAdjustForm = false;
    this.selectedItem   = null;
  }

  onHistoryClosed(): void {
    this.showHistoryDialog = false;
    this.selectedItem      = null;
  }
}