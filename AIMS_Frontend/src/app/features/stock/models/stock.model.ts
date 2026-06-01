import { ProductType } from '../../product/models/product-type.enum';

// ── Stock item hiển thị trên bảng ─────────────────────────────────────────────
export interface StockItem {
  productId: number;
  title: string;
  productType: ProductType;
  quantity: number;
}

// ── Loại điều chỉnh ───────────────────────────────────────────────────────────
export type AdjustType = 'IMPORT' | 'EXPORT' | 'DAMAGE' | 'CORRECTION';

// ── 1 lần điều chỉnh stock ───────────────────────────────────────────────────
export interface StockAdjustment {
  id: number;
  productId: number;
  adjustType: AdjustType;
  quantity: number;       // số lượng thay đổi (luôn dương)
  reason: string;
  adjustedAt: Date;
  adjustedBy?: string;
  quantityBefore: number;
  quantityAfter: number;
}