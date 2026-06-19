export interface DeliveryInfoRequest {
  recipientName: string;
  phoneNumber: string;
  email: string;
  deliveryProvince: string;
  detailAddress: string;
  note: string;
}

export interface CartItemRequest {
  productId: number;
  quantity: number;
}

export interface CalculateShippingRequest {
  deliveryProvince: string;
  items: CartItemRequest[];
}

export interface CreateInvoiceRequest {
  deliveryInfo: DeliveryInfoRequest;
  items: CartItemRequest[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface InvoiceLineResponse {
  productId: number;
  title: string;
  category: string;
  image: string;
  quantity: number;
  unitPriceExVat: number;
  amountExVat: number;
}

export interface InvoiceResponse {
  invoiceId: string;
  orderId: string;
  issueDate: string;
  items: InvoiceLineResponse[];
  subtotalExVat: number;
  vat: number;
  subtotalIncVat: number;
  shippingFee: number;
  total: number;
}

export interface InvoiceScreenLineItem {
  productId?: number;
  productTitle: string;
  category?: string;
  image?: string;
  quantity: number;
  unitSellingPrice: number;
  lineTotalSellingPrice: number;
}

export interface InvoiceScreenResponse {
  orderId: string;
  invoiceId: string;
  issueDate?: string;
  lineItems: InvoiceScreenLineItem[];
  totalProductPriceExclVat: number;
  totalProductPriceInclVat: number;
  deliveryFee: number;
  totalAmountToBePaid: number;
  recipientName?: string;
  phoneNumber?: string;
  email?: string;
  detailAddress?: string;
  province?: string;
}
