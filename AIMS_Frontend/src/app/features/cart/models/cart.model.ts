export interface CartItemView {
  productId: number;
  title: string;
  category: string;
  unitPriceExVat: number;
  quantity: number;
  availableQuantity?: number;
  image?: string;
}

export interface CartItemRequest {
  productId: number;
  quantity: number;
}

export interface CartSummary {
  itemCount: number;   // tổng số sản phẩm (sum of quantities)
  totalPrice: number;
}

export interface CartItem {
  productId: number;
  title: string;
  category: string;
  unitPriceExVat: number;
  quantity: number;
  image?: string;
}