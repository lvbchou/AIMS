export interface DeliveryInfoRequest {
  recipientName: string;
  phoneNumber: string;
  email: string;
  deliveryProvince: string;
  detailAddress: string;
  note: string;
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